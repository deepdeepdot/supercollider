Score {
	var <>score, routine, isPlaying = false;
	
	*new { arg list;
		^super.new.init(list);
	}
	
	*newFromFile { arg path;
		var list;
		list = thisProcess.interpreter.executeFile(path);
		^super.new.init(list);
	}
	
	init { arg list;
		score = list;
	}
	
	add { arg bundle;
		score = score.add(bundle)
	}
	
	*playFromFile { arg path, server;
		var list;
		list = thisProcess.interpreter.executeFile(path);
		^this.new(list).play(server);
	}
	
	*play { arg list, server;
		^this.new(list).play(server);
	}	
	
	play { arg server;
		var size, osccmd, timekeep, inserver, rout, sortedScore;
		sortedScore = score.quickSort({ arg a, b; a[0] < b[0] });
		isPlaying.not.if({
			inserver = server ? Server.default;
			size = sortedScore.size;
			timekeep = 0;
			routine = Routine({
				size.do { |i|
					var deltatime, msg;
					osccmd = sortedScore[i];
					deltatime = osccmd[0];
					msg = osccmd.copyToEnd(1);
					(deltatime-timekeep).wait;
					inserver.listSendBundle(inserver.latency, msg);
					timekeep = deltatime;
				};
				isPlaying = false;
			});
			isPlaying = true;
			routine.play;
		}, {"Score already playing".warn;}
		);
	}
	
	recordNRT { arg oscFilePath, outputFilePath, inputFilePath, sampleRate = 44100, headerFormat = 
		"AIFF", sampleFormat = "int16", options, completionString="";
		this.class.recordNRT(
			score, oscFilePath, outputFilePath, inputFilePath, 
			sampleRate, headerFormat, sampleFormat, options, completionString
		);
	}
	
	*recordNRT { arg list, oscFilePath, outputFilePath, inputFilePath, sampleRate = 44100, 
		headerFormat = "AIFF", sampleFormat = "int16", options, completionString="";
		this.write(list, oscFilePath);
		unixCmd("./scsynth -N" + oscFilePath + (inputFilePath ? "_") + outputFilePath + 			sampleRate + headerFormat + sampleFormat + 
			(options ?? {ServerOptions.new}).asOptionsString  
			+ completionString);
	}
	
	stop {
		isPlaying.if({routine.stop; isPlaying = false; routine = nil;}, {"Score not playing".warn;}
		);
	}
		
	*writeFromFile { arg path, oscFilePath;
		var list;
		list = thisProcess.interpreter.executeFile(path);
		this.write(list, oscFilePath);
	}
	
	*write {arg list, oscFilePath;
		var osccmd, f;
		f = File(oscFilePath, "w");
		list.size.do { |i|
			osccmd = list[i].asRawOSC;
			f.write(osccmd.size).write(osccmd);
		};
		f.close;
		"done".postln;
	}
	
	write {arg oscFilePath;
		this.class.write(score, oscFilePath);
	}
	
}	
