package edu.northeastern.group_project_group_8;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class PositionPrice {
    String positionName;
    ArrayList<Price> prices;

    public PositionPrice(String positionName) {
        this.positionName = positionName;
        this.prices = new ArrayList<Price>();
    }
}
