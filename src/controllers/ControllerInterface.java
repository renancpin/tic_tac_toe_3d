package controllers;

import game.instructions.InstructionConsumerInterface;

public interface ControllerInterface extends InstructionConsumerInterface {
    public void start();
}
