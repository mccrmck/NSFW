AkaiLPD8 : NS_Controller {
    classvar <connected = false;

    *connect {
        // make this better, search .devices by name and just connect to this one
        if(MIDIClient.initialized.not) 
        { MIDIClient.init; MIDIIn.connectAll } 
        { MIDIClient.list }; // refreshes list of MIDIEndPoints
        connected = true;
    }

    *cleanUp {
        connected = false;
    }

    *drawView {}

    *switchStripPage {}
    *save {}
    *load {}
}
