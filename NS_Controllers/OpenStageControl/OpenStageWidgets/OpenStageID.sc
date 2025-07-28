OpenStageID {}


// are we sure there's not a way to use inheritance here?

OpenStageButtonID : OpenStageID {
    classvar <>id = 0;
    *initClass { id = 0; }
    *next { ^id = id + 1 }
    *setID { |newId| id = newId }
}

OpenStageFaderID : OpenStageID { 
    classvar <>id = 0;
    *initClass { id = 0; }
    *next { ^id = id + 1; } 
    *setID { |newId| id = newId }
}

OpenStagePanelID : OpenStageID {
    classvar <>id = 0;
    *initClass { id = 0; }
    *next { ^id = id + 1; } 
    *setID { |newId| id = newId }
}

OpenStageXYID : OpenStageID {
    classvar <>id = 0;
    *initClass { id = 0; }
    *next { ^id = id + 1; } 
    *setID { |newId| id = newId }
}
