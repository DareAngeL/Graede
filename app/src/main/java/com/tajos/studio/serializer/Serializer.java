package com.tajos.studio.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;

/**
 *
 * @author Rene Tajos Jr.
 */
public class Serializer {
    
    public static class JSONColorSerializer extends JsonSerializer<Color> {
        @Override
        public void serialize(Color color, JsonGenerator gen, SerializerProvider sp) throws IOException {
             if (color == null) {
                gen.writeNull();
                return;
            }
            gen.writeNumber(color.getRGB());
        }
    }
    
    public static class JSONColorDeserializer extends JsonDeserializer<Color> {

        @Override
        public Color deserialize(JsonParser parser, DeserializationContext dc) throws IOException, JacksonException {
            return new Color(parser.getIntValue());
        }
    }
    
    public static class JSONRectanlgeSerializer extends JsonSerializer<Rectangle> {
        @Override
        public void serialize(Rectangle rect, JsonGenerator jg, SerializerProvider sp) throws IOException {
            if (rect == null) {
                jg.writeNull();
                return;
            }
            jg.writeStartArray();
            jg.writeNumber(rect.x);
            jg.writeNumber(rect.y);
            jg.writeNumber(rect.width);
            jg.writeNumber(rect.height);
            jg.writeEndArray();
        }
    }
    
    public static class JSONRectangleDeserializer extends JsonDeserializer<Rectangle> {
        @Override
        public Rectangle deserialize(JsonParser parser, DeserializationContext dc) throws IOException, JacksonException {
            
            if (parser.getCurrentToken() != JsonToken.START_ARRAY) {
                throw new JsonParseException(parser, "Start array expected", parser.getCurrentLocation());
            }
            
            int x = parser.nextIntValue(0);
            int y = parser.nextIntValue(0);
            int widht = parser.nextIntValue(0);
            int height = parser.nextIntValue(0);
            
            parser.nextToken();
            if (parser.getCurrentToken() != JsonToken.END_ARRAY) {
                throw new JsonParseException(parser, "End array expected", parser.getCurrentLocation());
            }
            
            return new Rectangle(x, y, widht, height);
        }
    }
}