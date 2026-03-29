package com.example.backend.controller;

import com.example.backend.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;



import java.util.NoSuchElementException;



import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @Test
    void getState_shouldReturnOk() throws Exception {
        given(gameService.getState("room1")).willReturn(null);

        mockMvc.perform(get("/api/game/room1/state"))
                .andExpect(status().isOk());
    }

    @Test
         void getConfig_shouldReturn404WhenRoomMissing() throws Exception {
        willThrow(new NoSuchElementException("Game not found"))
                     .given(gameService).getConfig("missing");

        mockMvc.perform(get("/api/game/missing/config"))
                .andExpect(status().is4xxClientError());
    }






    @Test
    void nextTurn_shouldReturnOk()       throws Exception {
        mockMvc.perform(post("/api/game/room1/next-turn")
                             .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player":1}
                                """))
                .andExpect(status().isOk());
    }
}