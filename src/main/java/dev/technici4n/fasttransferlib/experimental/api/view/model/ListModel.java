package dev.technici4n.fasttransferlib.experimental.api.view.model;

import dev.technici4n.fasttransferlib.experimental.api.view.Atom;

import java.util.List;

public interface ListModel
        extends Model {
    List<? extends Atom> getAtomList();
}
