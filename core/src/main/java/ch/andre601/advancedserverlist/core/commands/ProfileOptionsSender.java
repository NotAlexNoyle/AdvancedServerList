/*
 * MIT License
 *
 * Copyright (c) 2022-2025 Andre_601
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
 */

package ch.andre601.advancedserverlist.core.commands;

import ch.andre601.advancedserverlist.api.objects.NullBool;
import ch.andre601.advancedserverlist.core.interfaces.commands.CmdSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileOptionsSender{
    
    private final List<OptionStringBuilder> builders = new ArrayList<>();
    private final String file;
    
    public ProfileOptionsSender(String file){
        this.file = file;
    }
    
    public ProfileOptionsSender append(OptionStringBuilder builder){
        this.builders.add(builder.file(file));
        return this;
    }
    
    public void send(CmdSender sender){
        sender.sendMsg();
        sender.sendPrefixedMsg("Profile Info [<white>%s</white>]", file);
        for(OptionStringBuilder builder : builders){
            sender.sendMsg(builder.build());
        }
        sender.sendMsg();
        sender.sendMsg("[<grey>Hover for info, click for command</grey>]");
        sender.sendMsg();
    }
    
    public static class OptionStringBuilder{
        
        public static OptionStringBuilder of(String displayName, String hover){
            return new OptionStringBuilder(displayName)
                .hover(hover == null || hover.isEmpty() ? "<i>Not set</i>" : hover)
                .color(hover == null || hover.isEmpty() ? ColorName.RED : ColorName.GREEN);
        }
        
        public static OptionStringBuilder of(String displayName, List<String> hover){
            return new OptionStringBuilder(displayName)
                .hover(hover == null || hover.isEmpty() ? "<i>Not set</i>" : String.join("\n<reset>", hover))
                .color(hover == null || hover.isEmpty() ? ColorName.RED : ColorName.GREEN);
        }
        
        public static OptionStringBuilder of(String displayName, NullBool hover){
            return new OptionStringBuilder(displayName)
                .hover(hover == null || !hover.getOrDefault(false) ? "<red>Disabled</red>" : "<green>Enabled</green>")
                .color(hover == null || !hover.getOrDefault(false) ? ColorName.RED : ColorName.GREEN);
        }
        
        public static OptionStringBuilder of(String displayName, int hover){
            return new OptionStringBuilder(displayName)
                .hover(String.valueOf(hover));
        }
        
        public static OptionStringBuilder of(String displayName){
            return new OptionStringBuilder(displayName).hover(null);
        }
        
        private final String displayName;
        private String file = null;
        private String prefix = "├─";
        private ColorName color = ColorName.AQUA;
        private String hover = "<i>Not set</i>";
        private CmdProfiles.ProfileOption option = null;
        
        private OptionStringBuilder(String displayName){
            this.displayName = displayName;
        }
        
        public OptionStringBuilder file(String file){
            this.file = file;
            return this;
        }
        
        public OptionStringBuilder prefix(String prefix){
            this.prefix = prefix;
            return this;
        }
        
        public OptionStringBuilder color(ColorName color){
            this.color = color;
            return this;
        }
        
        public OptionStringBuilder hover(String hover){
            this.hover = hover;
            return this;
        }
        
        public OptionStringBuilder option(CmdProfiles.ProfileOption option){
            this.option = option;
            return this;
        }
        
        public String build(){
            StringBuilder builder = new StringBuilder("<grey>")
                .append(prefix)
                .append(" <white>[")
                .append('<')
                .append(color)
                .append('>');
            
            if(hover != null && !hover.isEmpty()){
                builder.append("<hover:show_text:\"")
                    .append(hover)
                    .append("\">");
            }
            
            if(option != null && file != null && !file.isEmpty()){
                builder.append("<click:suggest_command:/asl profiles set ")
                    .append(file)
                    .append(" ")
                    .append(option)
                    .append(" >");
            }
            
            builder.append(displayName);
            
            if(option != null && file != null && !file.isEmpty())
                builder.append("</click>");
            
            if(hover != null && !hover.isEmpty())
                builder.append("</hover>");
            
            builder.append("</")
                .append(color)
                .append(">]</white></grey>");
            
            return builder.toString();
        }
    }
    
    public enum ColorName{
        RED,
        GREEN,
        AQUA;
        
        @Override
        public String toString(){
            return this.name().toLowerCase(Locale.ROOT);
        }
    }
}
