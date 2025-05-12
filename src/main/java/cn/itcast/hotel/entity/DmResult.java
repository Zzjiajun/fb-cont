package cn.itcast.hotel.entity;

import lombok.Data;

import java.util.List;

@Data
public class DmResult {
    private Integer Id;
    private String link;
    private List<String> pixelList;
    private String detail;
    private String key;
    private boolean dataSuccess;
}
