package com.example.foodnote.backend;

import com.example.foodnote.backend.models.Recipe;

import com.example.foodnote.backend.models.Step;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

public class OfyService {

    static {
        ObjectifyService.register(Recipe.class);
        ObjectifyService.register(Step.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}
