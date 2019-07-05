package io.fenogy.comix.model;


import java.util.List;

/**
 * Created by prabashk on 5/23/2018.
 */

public class ComicCatalogueJSONModel {

    private List <ComicJSONModel> catalogue;


    public ComicCatalogueJSONModel() {

    }

    public ComicCatalogueJSONModel(List <ComicJSONModel> catalogue) {

        this.catalogue = catalogue;
    }

    public List<ComicJSONModel> getCatalogue() {
        return catalogue;
    }

    public void setCatalogue(List<ComicJSONModel> catalogue) {
        this.catalogue = catalogue;
    }





}
