package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class LabController {

    @GetMapping("/text")
    public String getText() {
        return "Бабашкин Тимофей Олегович БКС-2401";
    }

    @GetMapping("/numbers")
    public List<Integer> getNumbers() {
        return List.of(1, 2, 3, 4, 5);
    }
}