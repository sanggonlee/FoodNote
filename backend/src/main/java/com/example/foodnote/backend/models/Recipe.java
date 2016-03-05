package com.example.foodnote.backend.models;

import com.google.appengine.api.datastore.Blob;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.Date;
import java.util.List;

@Entity
public class Recipe {
    @Id
    private Long id;
    private String authorId;
    private String authorName;
    private String title;
    private String description;
    private String ingredients;
    private byte[] imageData;
    private List<String> steps;
    private Date date;

    public static class Builder {
        private final String title;

        private String description;
        private String ingredients;
        private List<String> steps;

        public Builder(String title) {
            this.title = title;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder ingredients(String ingredients) {
            this.ingredients = ingredients;
            return this;
        }

        public Builder steps(List<String> steps) {
            this.steps = steps;
            return this;
        }

        public Recipe build() {
            return new Recipe(this);
        }
    }

    /*
     *  No-arg constructor must be supplied for GAE entity
     */
    private Recipe() {
    }

    /*
     *  Constructor using Builder
     */
    private Recipe(Builder builder) {
        title = builder.title;
        description = builder.description;
        ingredients = builder.ingredients;
        steps = builder.steps;
    }

    /*
     *  Getters and setters
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] byteArray) {
        this.imageData = byteArray;
    }

    public List<String> getSteps() {
        return steps;
    }
/*
    public void setSteps(List<String> steps) {
        this.steps = steps;
    }
*/
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void something() {
        return;
    }
}
