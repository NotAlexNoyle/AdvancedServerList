package ch.andre601.advancedserverlist.core.placeholders;

import ch.andre601.advancedserverlist.api.PlaceholderProvider;
import ch.andre601.advancedserverlist.api.objects.GenericPlayer;
import ch.andre601.advancedserverlist.api.objects.GenericServer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Function;

public class DatePlaceholder extends PlaceholderProvider{
    
    public DatePlaceholder(){
        super("date");
    }
    
    @Override
    public @Nullable String parsePlaceholder(@NonNull String placeholder, @NonNull GenericPlayer player, @NonNull GenericServer server){
        String[] args = placeholder.split("\\s+");
        
        return switch(args[0]){
            case "current" -> {
                String format = "dd/MM/yyyy";
                if(args.length > 1){
                    format = format(Arrays.copyOfRange(args, 1, args.length));
                }
                
                try{
                    SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                    yield dateFormat.format(new Date());
                }catch(IllegalArgumentException ex){
                    yield "ERR: Invalid Date pattern " + format;
                }
            }
            case "isBefore" -> compareDates(args, integer -> integer < 0);
            case "isExactly" -> compareDates(args, integer -> integer == 0);
            case "isAfter" -> compareDates(args, integer -> integer > 0);
            default -> null;
        };
    }
    
    private String format(String... args){
        if(args.length == 0)
            return "dd/MM/yyyy";
        
        String format = String.join(" ", args);
        if(format.trim().isEmpty()){
            return "dd/MM/yyyy";
        }else{
            return format.trim();
        }
    }
    
    private String compareDates(String[] args, Function<Integer, Boolean> function){
        if(args.length == 1)
            return "ERR: No Date provided!";
        
        String targetDateStr = args[1];
        String format = "dd/MM/yyyy";
        if(args.length > 2){
            format = format(Arrays.copyOfRange(args, 2, args.length));
        }
        
        SimpleDateFormat dateFormat;
        try{
            dateFormat = new SimpleDateFormat(format);
            Date targetDate = dateFormat.parse(targetDateStr);
            int diff = new Date().compareTo(targetDate);
            
            return String.valueOf(function.apply(diff));
        }catch(IllegalArgumentException ex){
            return "ERR: Invalid Date format provided!";
        }catch(ParseException ex){
            return "ERR: Received invalid Date " + targetDateStr;
        }
    }
}
