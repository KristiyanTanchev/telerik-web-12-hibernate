package com.company.web.springdemo.controllers.mvc;

import com.company.web.springdemo.exceptions.EntityNotFoundException;
import com.company.web.springdemo.models.Beer;
import com.company.web.springdemo.services.BeerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/beers")
public class BeerMvcController {

    private final BeerService beerService;

    @Autowired
    public BeerMvcController(BeerService beerService) {
        this.beerService = beerService;
    }

    @GetMapping("/{id}")
    public String showBeer(Model model, @PathVariable int id){
        try {
            Beer beer = beerService.get(id);
            List<Beer> beers = new ArrayList<>();
            beers.add(beer);
            model.addAttribute("creatorEmail", beer.getCreatedBy().getEmail());
            model.addAttribute("beers", beers);
        }catch (EntityNotFoundException e){
            model.addAttribute("statusCode",
                    HttpStatus.NOT_FOUND.getReasonPhrase());
            return "ErrorView";
        }

        return "BeersView";
    }

    @GetMapping
    public String showBeers(Model model,
                            @RequestParam(required = false) String beerName,
                            @RequestParam(required = false) String styleName,
                            @RequestParam(required = false) Double minAbv,
                            @RequestParam(required = false) Double maxAbv,
                            @RequestParam(defaultValue = "name") String sortBy,
                            @RequestParam(defaultValue = "asc") String sortOrder){
        List<Beer> beers = beerService.get(beerName, minAbv, maxAbv, styleName, sortBy, sortOrder);

        model.addAttribute("beers", beers);

        return "BeersView";
    }

}
