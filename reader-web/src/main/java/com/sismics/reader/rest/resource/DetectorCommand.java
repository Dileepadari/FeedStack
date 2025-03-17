package com.sismics.reader.rest.resource;
import java.util.List;


interface DetectorCommand {
    String execute(List<String> articleIds, List<String> titles, List<String> descriptions,double threshold);
}
