NS_Waveform : NS_Widget {
    classvar selectionColors; 
    var mouseHandler;
    var <sfView;
    var draggable;
    var <currentSelection = 0;

    *initClass {
        selectionColors = (
            0: NS_Style('white').copy.alpha_(0.5),
            1: NS_Style('aqua').copy.alpha_(0.5),
            2: NS_Style('green').copy.alpha_(0.5),
            3: NS_Style('yellow').copy.alpha_(0.5),
            4: NS_Style('orange').copy.alpha_(0.5),
            5: NS_Style('pink').copy.alpha_(0.5),
            6: NS_Style('red').copy.alpha_(0.5),
            7: NS_Style('purple').copy.alpha_(0.5),
        )
    }

    *new { ^super.new.drawWidget }

    drawWidget {
        mouseHandler = UserView()
        .mouseDownAction_({ |...args| 
            draggable = true;
            this.onMouseDown(*args)
        })
        .mouseUpAction_({ draggable = false });

        sfView = SoundFileView()
        .drawsBoundingLines_(false)
        .peakColor_(NS_Style('highlight'))
        .rmsColor_(NS_Style('blue'))
        .background_(NS_Style('transparent'))
        .timeCursorOn_(true)
        .timeCursorColor_(NS_Style('yellow')) 
        .drawsWaveForm_(true)
        .gridOn_(false);

        selectionColors.keysValuesDo { |k, v| sfView.setSelectionColor(k, v) };

        view = UserView()
        .drawFunc_({ |v|
            var w = v.bounds.width;
            var h = v.bounds.height;
            var r = NS_Style('radius');
            var b = NS_Style('border');

            Pen.strokeColor_(NS_Style('bGroundDark'));
            Pen.fillColor_(NS_Style('bGroundDark'));
            Pen.width_(b);
            Pen.addRoundedRect(Rect(0, 0, w, h).insetBy(b / 2), r, r);
            Pen.fillStroke
        })
        .layout_(
            HLayout(
                UserView().layout_(
                    StackLayout(mouseHandler, sfView).mode_(\stackAll)
                )
            ).nsMarginsSpacing('view')
        );

        // set time cursor => unit: frames
        this.addLeftClickAction({ |nsWave, mouseView, x, y|
            var cursorPos = this.prCalcX(x, mouseView);
            sfView.timeCursorPosition_(cursorPos);
        });

        // drag to set current selection => unit: frames
        mouseHandler.mouseMoveAction_({ |mouseView, x, y, mod|
            var dragEdge = this.prCalcX(x, mouseView);
            var cursor = sfView.timeCursorPosition;

            // the last condition is to prevent losing regions when setting the
            // cursor position, I probably don't need selection 0.1s long...
            if(draggable && { mod == 0 } && { (dragEdge - cursor).abs > 4800 }) {
                if(dragEdge > sfView.timeCursorPosition)
                { sfView.setSelection(currentSelection, [cursor, dragEdge - cursor]) } 
                { sfView.setSelection(currentSelection, [dragEdge, cursor - dragEdge]) }
            }
        });

        // zoom on the x-axis => unit: seconds
        // scroll on the x-axis is also possible, can be improved
        mouseHandler.mouseWheelAction_({ |mouseView, x, y, mod, xDelta, yDelta|
            if(mod.isCtrl) {
                var viewSecs = sfView.xZoom;                // seconds
                var zoom = (viewSecs * 0.01) * yDelta.sign; // seconds
                var xZoom = viewSecs - zoom;                // seconds
                var duration = sfView.soundfile.duration;   // seconds
                sfView.xZoom_(xZoom.min(duration));         // seconds
                // this should be better, maybe scroll to cursor position?
                sfView.scroll(0.01 * xDelta.sign)
            }
        });
    }

    // returns postion (in frames) in soundfile accounting for zoom and scroll
    prCalcX { |x, view|
        var sfFrames = sfView.numFrames;
        var vFrames  = sfView.viewFrames;
        var offset   = sfView.scrollPos * (sfFrames - vFrames);
        var xPos     = (x / view.bounds.width).clip(0, 1);
        ^(xPos * vFrames) + offset;
    }

    loadSoundFile { |path|
        SoundFile.use(path, { |f|
            sfView.soundfile =  f;
            sfView.read(0, f.numFrames)
        })
    }

    cursorPos { ^sfView.timeCursorPosition }

    cursorPos_ { |frame|
        { sfView.timeCursorPosition_(frame) }.defer
    }

    currentSelection_ { |index|
        if(index > 7) 
        { "selection index out of range".error }
        { currentSelection = index }
    }

    cueSelection { |index| 
        if(index > 7) 
        { "selection index out of range".error }
        { sfView.timeCursorPosition_(sfView.selectionStart(index)) }
    }
}
