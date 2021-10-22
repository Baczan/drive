package com.baczan.session_authorization_server.service;

import com.baczan.session_authorization_server.exceptions.TierNotFoundException;
import com.baczan.session_authorization_server.helpers.Tier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


public class TierService {

    private List<Tier> tierList = new ArrayList<>();


    public TierService() {
    }

    public Tier getTierByName(String name) throws TierNotFoundException {
        return tierList.stream()
                .filter(tier -> tier.getTierName().equals(name))
                .findAny().orElseThrow(TierNotFoundException::new);
    }

    public Tier getTierByPrice(String price) throws TierNotFoundException {
        return tierList.stream()
                .filter(tier -> tier.getStripePriceId().equals(price))
                .findAny().orElseThrow(TierNotFoundException::new);
    }

    public void add(Tier tier){
        tierList.add(tier);
    }

}
