OpenStageID {
    classvar buttonId, faderId, panelId, xyId;

    *initClass {
        buttonId = faderId = panelId = xyId = 0
    }

    *next { |key|
        key.switch(
            'button', { ^buttonId = buttonId + 1 },
            'fader',  { ^faderId  = faderId + 1 },
            'panel',  { ^panelId  = panelId + 1 },
            'xy',     { ^xyId     = xyId + 1 },
        );
    }

    *save { ^[buttonId, faderId, panelId, xyId] }

    *load { |loadArray|
        buttonId = loadArray[0];
        faderId  = loadArray[1];
        panelId  = loadArray[2]; 
        xyId     = loadArray[3];
    }
}
