/*
Copyright (c) 2009-2021, Andrew M. Martin
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
   disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of Pandam nor the names of its contributors may be used to endorse or promote products derived from this
   software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/
package org.pandcorps.pandam.android;

import java.io.*;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;

public final class AndroidPanaudio extends Panaudio {
	@Override
	public final Pansound createSound(final String location) {
		return new SoundPoolPansound(location);
	}
	
	@Override
	public final Pansound createMusic(final String location) {
	    if (location.endsWith(".jet")) {
	        return new JetPansound(location);
	    }
	    final String jetLocation = JetPansound.toJetLocation(location);
	    InputStream in = null;
        try {
            in = Iotil.getResourceInputStream(jetLocation);
            if (in != null) {
                return new JetPansound(jetLocation, in);
            }
        } catch (final Exception e) {
            // No .jet version available, just use the given location
        } finally {
            Iotil.close(in);
        }
        if (location.endsWith(".mid")) {
            return new JetPansound(location);
        }
		return new MediaPlayerPansound(location);
	}
	
	@Override
	public final Pansound createTransition(final String location) {
		return new MediaPlayerPansound(location);
	}

	@Override
	protected final void setEnabled(final boolean music, final boolean enabled) {
		if (enabled) {
			return;
		} else if (music) {
		    final Pansound currentMusic = getMusic();
		    if (currentMusic instanceof MediaPlayerPansound) {
		        ((MediaPlayerPansound) currentMusic).stop();
		    }
		    if (JetPansound.jetPlayer != null) {
    			JetPansound.jetPlayer.pause();
    			JetPansound.jetPlayer.clearQueue();
    			if (!JetPansound.jetPlayer.closeJetFile()) {
    				throw new Panception("Failed to close Jet file");
    			}
		    }
		}
	}
	
	protected static Pansound pausedMusic = null;
	
	@Override
	public final void pauseMusic() {
		pausedMusic = getMusic();
		if (pausedMusic == null) {
			return;
		} else if (pausedMusic instanceof MediaPlayerPansound) {
		    ((MediaPlayerPansound) pausedMusic).pause();
		} else if (JetPansound.jetPlayer != null) {
		    JetPansound.jetPlayer.pause();
		}
	}
	
	@Override
	public final void resumeMusic() throws Exception {
		final Pansound oldPaused = pausedMusic;
		pausedMusic = null;
		if (!isMusicEnabled() || oldPaused == null || oldPaused != getMusic()) {
			return;
		} else if (oldPaused instanceof MediaPlayerPansound) {
		    ((MediaPlayerPansound) oldPaused).resume();
		} else if (JetPansound.jetPlayer != null) {
		    JetPansound.jetPlayer.play();
		}
	}

	@Override
	public final void stop() {
		setEnabled(false, false);
		setEnabled(true, false);
	}
	
	@Override
	public final void close() {
		if (JetPansound.jetPlayer != null) {
			JetPansound.jetPlayer.release();
    	}
		if (SoundPoolPansound.soundPool != null) {
			SoundPoolPansound.soundPool.release();
			SoundPoolPansound.soundPool = null;
    	}
	}
}
