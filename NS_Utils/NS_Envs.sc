NS_Envs {

  *new { |gate, pauseGate, amp|
    ^super.new.init(gate, pauseGate, amp)
  }

  init { |gate, pauseGate, amp|
    var env = Env.asr(0.02,1,0.02).ar(2,gate);
    var pauseEnv =  Env.asr(0.01,1,0.01).ar(1,pauseGate);
    ^env * pauseEnv * amp;
  }
}
