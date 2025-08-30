NS_Envs {

  *new { |sig, gate, pauseGate, amp|
    ^super.new.init(sig, gate, pauseGate, amp)
  }

  init { |sig, gate, pauseGate, amp|
    var env = Env.asr(0.01, 1, 0.01).ar(2, gate);
    var pauseEnv =  Env.asr(0.01, 1, 0.01).ar(1, pauseGate);
    ^sig * env * pauseEnv * amp;
  }
}
