package dev.technici4n.fasttransferlib.api.view.model;

import dev.technici4n.fasttransferlib.api.content.Content;
import dev.technici4n.fasttransferlib.api.view.Atom;

import java.util.Map;

public interface MapModel
        extends Model {
    Map<Content, ? extends Atom> getAtomMap();
}
