NS_UnitShape {
    classvar <helperFunctions, <unitShapers, <easingFuncs, <interpFuncs;

    *initClass {

        helperFunctions = (
            triangle: { |phase, skew|
                var warpedPhase = Select.ar(BinaryOpUGen('>', phase, skew), [
                    phase / skew,
                    1 - ((phase - skew) / (1 - skew))
                ]);
                Select.ar(BinaryOpUGen('==', skew, 0), [warpedPhase, 1 - phase]);
            },

            kink: { |phase, skew|
                var warpedPhase = Select.ar(BinaryOpUGen('>', phase, skew), [
                    0.5 * (phase / skew),
                    0.5 * (1 + ((phase - skew) / (1 - skew)))
                ]);
                Select.ar(BinaryOpUGen('==', skew, 0), [warpedPhase,  0.5 * (1 + phase)]);
            }
        );

        unitShapers = (
            hanning: { |phase| 1 - cos(phase * pi) * 0.5 },

            circular: { |phase| sqrt(phase * (2 - phase)) },

            welch: { |phase| (1 - squared(phase - 1)) },

            raisedCos: { |phase, index|
                var cosine = cos(phase * pi);
                exp(index.abs * (cosine.neg - 1));
            },

            gaussian: { |phase, index|
                var cosine = cos(phase * 0.5pi) * index;
                exp(cosine * cosine.neg);
            },

            trapezoid: { |phase, width, duty = 1|
                var sustain   = 1 - width;
                var offset    = phase - (1 - duty);
                var trapezoid = (offset / sustain + (1 - duty)).clip(0, 1);
                var pulse     = offset > 0;
                Select.ar(BinaryOpUGen('==', width, 1), [trapezoid, pulse]);
            },
        );

        easingFuncs = (
            cubic: { |x| x.pow(3) },

            quintic: { |x| x.pow(5) },

            sine: { |x| 1 - cos(x * 0.5pi) },

            circular: { |x| 1 - sqrt(1 - (x * x)) },

            pseudoExp: { |x, coef = 13| 
                (2 ** (coef * x) - 1) / (2 ** coef - 1)
            },

            pseudoLog2: { |x, coef = 12.5| 
                1 - (log2((1 - x) * (2 ** coef - 1) + 1) / coef)
            }
        );

        interpFuncs = (
            step:         { |x| x },
            smoothStep:   { |x| x * x * (3 - (2 * x)) },
            smootherStep: { |x| x * x * x * (x * (6 * x - 15) + 10) }
        );
    }

    /* ==== waveshapers ==== */

    *sigmoid { |x, curve|
        var safeDenom = max(1 - curve, 0.0001);
        var k = 2 * curve / safeDenom;
        ^((1 + k) * x / (1 + (k * x.abs)));
    }

    /* ==== easing funcs ==== */
    *easeIn { |x, key = \cubic ...args|
        var easeFunc = easingFuncs.atFail(key, { ^"easing function not found".warn });
        ^easeFunc.(x, *args)
    }

    *easeOut { |x, key = \cubic ...args|
        var easeFunc = easingFuncs.atFail(key, { ^"easing function not found".warn });
        ^(1 - easeFunc.(1 - x, *args))
    }

    // offset param seems funky...
    *easeInOut { |x, offset = 0.5, key = \cubic|
        var easeFunc = easingFuncs.atFail(key, { ^"easing function not found".warn });
        ^Select.ar(x > offset, [
            offset * easeFunc.(x / offset),
            offset + ((1 - offset) * (1 - easeFunc.((1 - x) / (1 - offset))))
        ]);
    }

    *easeOutIn { |x, height = 0.5, key = \cubic|
        var easeFunc = easingFuncs.atFail(key, { ^"easing function not found".warn });
        ^Select.ar(x > height, [
            height * (1 - easeFunc.((height - x) / height)),
            height + ((1 - height) * easeFunc.((x - height) / (1 - height)))
        ]);
    }

    /* ==== interp funcs ==== */

    *interpEasing { |x, shape, easingFuncA, easingFuncB, interp = \step|

        var interpFunc = interpFuncs.atFail(interp,{ ^"interp function not found".warn });

        var easingToLinear = { |x, shape, easingFunc, interpFunc|
            var mix = shape * 2;
            var mixInterp = interpFunc.(mix);
            easingFunc * (1 - mixInterp) + (x * mixInterp);
        };

        var linearToEasing = { |x, shape, easingFunc, interpFunc|
            var mix = (shape - 0.5) * 2;
            var mixInterp = interpFunc.(mix);
            x * (1 - mixInterp) + (easingFunc * mixInterp);
        };

        ^Select.ar(BinaryOpUGen('>', shape, 0.5), [
            easingToLinear.(x, shape, easingFuncA, interpFunc),
            linearToEasing.(x, shape, easingFuncB, interpFunc)
        ]);
    }

    *interpQuintic { |x, shape, interp = \step|
        var easeOut = this.easeOut(x, 'quintic');
        var easeIn  = this.easeIn(x, 'quintic');
        ^this.interpEasing(x, shape, easeOut, easeIn, interp)
    }

    *interpPseudoExp { |x, shape, interp = \step|
        var easeOut = this.easeOut(x, 'pseudoExp');
        var easeIn  = this.easeIn(x, 'pseudoExp');
        ^this.interpEasing(x, shape, easeOut, easeIn, interp)
    }

    *sigmoidToSeat { |x, shape, inflection = 0.5, interp = \step| 
        var easeOut = this.easeInOut(x, inflection, 'quintic');
        var easeIn  = this.easeOutIn(x, inflection, 'quintic');
        ^this.interpEasing(x, shape, easeOut, easeIn, interp)
    }

    /* ==== window funcs ==== */

    *hanningWin { |phase, skew|
        var warpedPhase = helperFunctions['triangle'].(phase, skew);
        ^unitShapers['hanning'].(warpedPhase)
    }

    *circularWin { |phase, skew|
        var warpedPhase = helperFunctions['triangle'].(phase, skew);
        ^unitShapers['circular'].(warpedPhase) 
    }

    *welchWin { |phase, skew|
        var warpedPhase = helperFunctions['triangle'].(phase, skew);
        ^unitShapers['welch'].(warpedPhase) 
    }

    *raisedCosWin { |phase, skew, index|
        var warpedPhase = helperFunctions['triangle'].(phase, skew);
        var raisedCos   = unitShapers['raisedCos'].(warpedPhase, index);
        var hanning     = unitShapers['hanning'].(warpedPhase);
        ^(raisedCos * hanning)
    }

    *gaussianWin { |phase, skew, index|
        var warpedPhase = helperFunctions['triangle'].(phase, skew);
        var gaussian    = unitShapers['gaussian'].(warpedPhase, index);
        var hanning     = unitShapers['hanning'].(warpedPhase);
        ^(gaussian * hanning)
    }

    *trapezoidWin { |phase, skew, width, duty = 1|
        var warpedPhase = helperFunctions['triangle'].(phase, skew);
        ^unitShapers['trapezoid'].(warpedPhase, width, duty)
    }

    *tukeyWin { |phase, skew, width, duty = 1|
        var warpedPhase = helperFunctions['triangle'].(phase, skew);
        var trapezoid   = unitShapers['trapezoid'].(warpedPhase, width, duty);        
        ^unitShapers['hanning'].(trapezoid)
    }

    *exponentialWin { |phase, skew, shape, interp = \step|
        var warpedPhase = helperFunctions['triangle'].(phase, skew);
        ^this.interpPseudoExp(warpedPhase, 1 - shape, interp)
    }
}
