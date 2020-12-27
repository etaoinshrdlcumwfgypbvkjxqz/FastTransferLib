package dev.technici4n.fasttransferlib.api.view.model;

import dev.technici4n.fasttransferlib.api.view.View;

public interface InputOutputModel
        extends Model {
    View getInput();

    View getOutput();

    View getProcess();
}
