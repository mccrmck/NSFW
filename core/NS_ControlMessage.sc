NS_ControlMessage {
    classvar <last;

    *initClass {
        last = Array.newClear(10)
    }

    *register { |...args|
        last = last.rotate;
        last[0] = args;
        last.postln;
    }

    // consider: write to a File for infinite undo?
}
