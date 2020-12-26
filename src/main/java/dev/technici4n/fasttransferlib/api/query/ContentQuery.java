package dev.technici4n.fasttransferlib.api.query;

import dev.technici4n.fasttransferlib.api.content.Content;

public interface ContentQuery
        extends CategoryQuery {
    Content getContent();
}
