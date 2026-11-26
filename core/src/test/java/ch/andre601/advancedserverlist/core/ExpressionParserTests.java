/*
 * MIT License
 *
 * Copyright (c) 2022-2024 Andre_601
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

package ch.andre601.advancedserverlist.core;

import ch.andre601.advancedserverlist.api.objects.GenericPlayer;
import ch.andre601.advancedserverlist.api.objects.GenericServer;
import ch.andre601.expressionparser.DefaultExpressionParserEngine;
import ch.andre601.expressionparser.ParseWarnCollector;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionParserTests{
    
    private final DefaultExpressionParserEngine parser = DefaultExpressionParserEngine.createDefault();
    private final Map<String, Boolean> testValuesMap = new HashMap<>(){{
        put("20 > 10", true);
        put("10 > 20", false);
        put("10 = 10", true);
        put("10 = 20", false);
        put("(10 - 5) > (1 + 1)", true);
        put("(10 - 10) > (1 + 1)", false);
        put("100 > 50 and 50 < 100", true);
        put("100 < 50 and 50 > 100", false);
        put("true or 100 > 10", true);
        put("false or 100 < 10", false);
    }};
    
    private final TestPlayer player = new TestPlayer();
    private final TestServer server = new TestServer();
    
    @Test
    public void testBasicExpressionResults(){
        System.out.println("====[ DefaultExpressionParser Tests ]====");
        for(Map.Entry<String, Boolean> values : testValuesMap.entrySet()){
            System.out.printf("| Test:     '%s'%n", values.getKey());
            System.out.printf("| Expected: %b%n", values.getValue());
            
            ParseWarnCollector collector = new ParseWarnCollector(values.getKey());
            boolean result = parser.compileToBoolean(values.getKey(), collector, player, server);
            
            System.out.printf("| Result:   %b%n", result);
            System.out.println("-----------------------------------------");
            
            assertEquals(result, values.getValue());
        }
    }
    
    private static class TestPlayer implements GenericPlayer{
        @Override
        public @NonNull String getName(){
            return "Lorem";
        }
        
        @Override
        public int getProtocol(){
            return 1;
        }
        
        @Override
        public @NonNull UUID getUUID(){
            return UUID.randomUUID();
        }
    }
    
    private static class TestServer implements GenericServer{
        @Override
        public int getPlayersOnline(){
            return 10;
        }
        
        @Override
        public int getPlayersMax(){
            return 20;
        }
        
        @Override
        public @Nullable String getHost(){
            return "play.example.com";
        }
    }
}
