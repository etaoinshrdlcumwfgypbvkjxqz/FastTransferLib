package dev.technici4n.fasttransferlib.api.query;

import net.fabricmc.fabric.api.util.TriState;

public interface Queryable {
    TriState query(Query query);
}
