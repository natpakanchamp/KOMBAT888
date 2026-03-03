package com.example.backend.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List ;
@Data
public class GameState {

//    public static final int ROW = 8 ;
//    public static final int COL = 8 ;

    private int turnCount  = 1  ;
    private int p1Budget = 10000;
    private int p2Budget = 10000;

    // List ทั้งหมด ในเกม
    private List<Unit> units  =  new ArrayList<>() ;

    // ถูกเรียกจาก game engine
    public void nextTurn()
    {
        turnCount++;
    }
}
