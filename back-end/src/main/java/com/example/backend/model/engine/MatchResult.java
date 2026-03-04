package com.example.backend.model.engine;

public enum MatchResult {
    ONGOING,       // เกมยังดำเนินอยู่
    PLAYER1_WINS,  // Player 1 ชนะ
    PLAYER2_WINS,  // Player 2 ชนะ
    DRAW           // เสมอ (ตายพร้อมกัน)
}
