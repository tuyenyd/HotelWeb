package com.example.hotel.controller;

import com.example.hotel.dto.PriceRuleDto;
import com.example.hotel.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/price-rules")
@RequiredArgsConstructor
public class PriceRuleAdminController {

    private final PriceService priceService;

    @GetMapping
    public ResponseEntity<List<PriceRuleDto>> getAllPriceRules() {
        return ResponseEntity.ok(priceService.getAllPriceRules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PriceRuleDto> getPriceRuleById(@PathVariable Long id) {
        return ResponseEntity.ok(priceService.getPriceRuleById(id));
    }

    @PostMapping
    public ResponseEntity<PriceRuleDto> createPriceRule(@RequestBody PriceRuleDto priceRuleDto) {
        return new ResponseEntity<>(priceService.createPriceRule(priceRuleDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PriceRuleDto> updatePriceRule(@PathVariable Long id, @RequestBody PriceRuleDto priceRuleDto) {
        return ResponseEntity.ok(priceService.updatePriceRule(id, priceRuleDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePriceRule(@PathVariable Long id) {
        priceService.deletePriceRule(id);
        return ResponseEntity.noContent().build();
    }
}