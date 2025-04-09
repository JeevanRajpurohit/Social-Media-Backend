package com.example.Social_Media_Platform.Util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationResponse {
    private Object data;
    private String lastEvaluatedKey;
    private int limit;
    private boolean hasMore;
}
