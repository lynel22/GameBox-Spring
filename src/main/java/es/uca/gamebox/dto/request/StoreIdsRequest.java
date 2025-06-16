package es.uca.gamebox.dto.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class StoreIdsRequest {
    private List<UUID> storeIds;
}
