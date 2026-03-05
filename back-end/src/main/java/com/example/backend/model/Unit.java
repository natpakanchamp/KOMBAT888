package com.example.backend.model;

// for getter , setter
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor // for create constructor
public class Unit {
    // type of unit
    private int type ;

    // position x in board
    private int x ;

    private int y ;



}
