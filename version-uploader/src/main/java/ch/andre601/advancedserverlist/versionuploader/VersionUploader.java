/*
 * MIT License
 *
 * Copyright (c) 2022-2023 Andre_601
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package ch.andre601.advancedserverlist.versionuploader;

import ch.andre601.advancedserverlist.versionuploader.data.CodebergRelease;
import ch.andre601.advancedserverlist.versionuploader.data.CodebergReleaseFetcher;
import ch.andre601.advancedserverlist.versionuploader.hangar.HangarVersionUploader;
import ch.andre601.advancedserverlist.versionuploader.modrinth.ModrinthVersionUploader;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.receive.ReadonlyMessage;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;

public class VersionUploader{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionUploader.class);
    
    public static void main(String[] args) {
        LOGGER.info("Starting release uploader...");
        
        if(args.length == 0){
            LOGGER.warn("MISSING ARGUMENT!");
            LOGGER.warn("Usage: java -jar VersionUploader.jar <help|upload <platform> [--dryrun]>");
            System.exit(1);
            return;
        }
        
        if(args[0].equalsIgnoreCase("help")){
            LOGGER.info("Usage: java -jar VersionUploader.jar <command>");
            LOGGER.info("");
            LOGGER.info("Commands:");
            LOGGER.info("  help                          Prints this help page and exits.");
            LOGGER.info("  upload <platform> [--dryrun]  Uploads to the specified platform.");
            LOGGER.info("");
            LOGGER.info("Upload Arguments:");
            LOGGER.info("  <platform>  The platforms to upload to. Allowed are all, modrinth and hangar.");
            LOGGER.info("  --dryrun    Do not upload and instead print the generated JSON.");
            System.exit(0);
            return;
        }
        
        if(!args[0].equalsIgnoreCase("upload")){
            LOGGER.warn("Unknown command {}.", args[0]);
            LOGGER.warn("Use 'java -jar VersionUploader.jar help' for available commands.");
            System.exit(1);
            return;
        }
        
        CodebergRelease release = CodebergReleaseFetcher.fetch();
        
        if(release == null){
            LOGGER.warn("Couldn't fetch Codeberg Release information!");
            System.exit(1);
            return;
        }
        
        boolean dryrun = false;
        if(args.length >= 3){
            dryrun = args[2].equalsIgnoreCase("--dryrun");
        }
        
        CompletableFuture<?> future;
        ReleaseHolder releaseHolder = ReleaseHolder.create();
        switch(args[1].toLowerCase(Locale.ROOT)){
            case "modrinth" -> future = new ModrinthVersionUploader().performUpload(release, releaseHolder, dryrun);
            case "hangar" -> future = new HangarVersionUploader().performUpload(release, releaseHolder, dryrun);
            case "all" -> future = CompletableFuture.allOf(
                new ModrinthVersionUploader().performUpload(release, releaseHolder, dryrun),
                new HangarVersionUploader().performUpload(release, releaseHolder, dryrun)
            );
            default -> {
                LOGGER.warn("Unknown argument '{}' provided.", args[1]);
                LOGGER.warn("Usage: java -jar VersionUploader.jar upload <all|hangar|modrinth> [--dryrun]");
                System.exit(1);
                return;
            }
        }
        
        if(future == null){
            LOGGER.warn("Issue while performing upload. Received CompletableFuture is null.");
            System.exit(1);
            return;
        }
        
        try{
            future.join();
            if(dryrun){
                LOGGER.info("Dryrun complete!");
            }else{
                LOGGER.info("Upload completed!");
            }
        }catch(Exception ex){
            LOGGER.warn("Upload was not successful! Encountered an Exception!", ex);
            System.exit(1);
        }
        
        if(releaseHolder.releases().isEmpty()){
            LOGGER.warn("No releases to share on Discord... Skipping.");
            return;
        }
        
        String webhookUrl = System.getenv("DISCORD_WEBHOOK_URL");
        if(webhookUrl == null || webhookUrl.isEmpty()){
            LOGGER.warn("No valid DISCORD_WEBHOOK_URL Environment Variable found. Skipping Webhook sending.");
            return;
        }
        
        WebhookEmbedBuilder builder = new WebhookEmbedBuilder()
            .setTitle(new WebhookEmbed.EmbedTitle("New Release", null))
            .setDescription(String.format(
                "Version `%s` of AdvancedServerList is now available for download on Modrinth and Hangar!",
                release.tagName()
            ))
            .setColor(0x1BD96A)
            .addField(new WebhookEmbed.EmbedField(
                false,
                "Changelog",
                getDescription(release.body())
            ));
        
        StringJoiner modrinthText = new StringJoiner("\n");
        String hangarText = null;
        for(ReleaseHolder.ReleaseInfo info : releaseHolder.releases()){
            String text = String.format("[%s](%s)", info.platform(), info.url());
            
            if(info.type().equalsIgnoreCase("modrinth")){
                modrinthText.add(text);
            }else{
                hangarText = text;
            }
        }
        
        builder.addField(new WebhookEmbed.EmbedField(
            true,
            "Modrinth:",
            modrinthText.toString()
        )).addField(new WebhookEmbed.EmbedField(
            true,
            "Hangar:",
            hangarText == null ? "No download available" : hangarText
        ));
        
        try(WebhookClient client = new WebhookClientBuilder(webhookUrl).build()){
            CompletableFuture<ReadonlyMessage> webhookFuture = client.send(builder.build()).whenComplete((readonlyMessage, throwable) -> {
                if(throwable != null){
                    LOGGER.warn("Error while sending webhook message!", throwable);
                    System.exit(1);
                    return;
                }
                
                LOGGER.info("Webhook message send!");
                client.close();
            });
            
            try{
                webhookFuture.join();
            }catch(Exception ex){
                LOGGER.warn("Unable to complete webhook message sending!", ex);
                System.exit(1);
            }
        }catch(Exception ex){
            LOGGER.warn("Encountered Exception while creating WebhookClient!", ex);
            System.exit(1);
        }
    }
    
    public static String retrieveVersion(){
        try(InputStream is = VersionUploader.class.getResourceAsStream("/version.properties")){
            Properties properties = new Properties();
            
            properties.load(is);
            
            return properties.getProperty("version");
        }catch(IOException ex){
            return null;
        }
    }
    
    private static String getDescription(String description){
        if(description.length() < 1024)
            return description;
        
        String temp = description.substring(0, 1014); // Cut it 10 characters shorter than supported.
        
        return temp.substring(0, temp.lastIndexOf(' ')) + "...";
    }
}
