package searchengine.dto.statistics;

import lombok.Value;

@Value
public class BadRequestDto {
    boolean getResult;
    String errorMessage;

}
