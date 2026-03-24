package com.example.backend.dto;

import com.example.backend.model.engine.MatchResult ;
import java.util.List ;


public record GameSummaryDto (String winner , String loser , int totalTurn ,
                              playerSummary player1 , playerSummary player2  ) {

    public record playerSummary(
            long remainingBudget , // Budget คงเหลือ
            int remainingMinion , // จำนวน minion คงเหลือ
            int totalHp ,   // รวม HP ของ minion ทั้งหมดที่รอด
            int ownedHexs ,  // จำนวน hex ของ แต่ละ plyer
            int turn
    ){}


}
