package com.ftn.sbnz.kjar.seed;

import com.ftn.sbnz.model.enums.VezaType;
import com.ftn.sbnz.model.facts.IngredientGroup;
import com.ftn.sbnz.model.facts.Veza;

import java.util.ArrayList;
import java.util.List;

public final class IngredientGraph {

    public static final String RETINOIDI = "retinoidi";
    public static final String TETRACIKLINI = "tetraciklini";
    public static final String TOPIJSKI_KORTIKOSTEROIDI = "topijski kortikosteroidi";

    private IngredientGraph() {
    }

    public static List<IngredientGroup> groups() {
        return List.of(
                new IngredientGroup("isothiazolinones", true),
                new IngredientGroup("para group", true),
                new IngredientGroup("parabens", true),
                new IngredientGroup("fragrance mix i", false),
                new IngredientGroup(RETINOIDI, false),
                new IngredientGroup(TETRACIKLINI, false),
                new IngredientGroup(TOPIJSKI_KORTIKOSTEROIDI, false));
    }

    public static List<Veza> edges() {
        List<Veza> edges = new ArrayList<>();

        contains(edges, "kathon cg", "methylisothiazolinone", "methylchloroisothiazolinone");
        memberOf(edges, "isothiazolinones", "methylisothiazolinone", "methylchloroisothiazolinone");

        releases(edges, "formaldehyde", "dmdm hydantoin", "quaternium-15", "imidazolidinyl urea");

        memberOf(edges, "fragrance mix i", "cinnamal", "eugenol", "isoeugenol", "geraniol");
        memberOf(edges, "parabens", "methylparaben", "propylparaben");
        memberOf(edges, "para group", "p-phenylenediamine", "benzocaine");

        memberOf(edges, RETINOIDI, "tretinoin", "adapalen", "retinol", "tazaroten");
        memberOf(edges, TETRACIKLINI, "doksiciklin", "minociklin");
        memberOf(edges, TOPIJSKI_KORTIKOSTEROIDI, "hidrokortizon", "betametazon", "mometazon", "klobetazol");

        return edges;
    }

    private static void contains(List<Veza> edges, String product, String... ingredients) {
        for (String ingredient : ingredients) {
            edges.add(new Veza(product, ingredient, VezaType.CONTAINS));
        }
    }

    private static void memberOf(List<Veza> edges, String group, String... members) {
        for (String member : members) {
            edges.add(new Veza(member, group, VezaType.MEMBER_OF));
        }
    }

    private static void releases(List<Veza> edges, String released, String... releasers) {
        for (String releaser : releasers) {
            edges.add(new Veza(releaser, released, VezaType.RELEASES));
        }
    }
}
