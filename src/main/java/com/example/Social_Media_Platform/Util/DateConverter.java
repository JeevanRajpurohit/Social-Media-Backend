package com.example.Social_Media_Platform.Util;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateConverter implements DynamoDBTypeConverter<String, Date> {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public String convert(Date date) {
        synchronized (DATE_FORMAT) {
            return DATE_FORMAT.format(date);
        }
    }

    @Override
    public Date unconvert(String dateString) {
        synchronized (DATE_FORMAT) {
            try {
                return DATE_FORMAT.parse(dateString);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Unable to parse date", e);
            }
        }
    }
}