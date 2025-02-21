/*
 * Copyright (c) 2016-2018, Seth <Sethtroll3@gmail.com>
 * Copyright (c) 2020, ConorLeckey <https://github.com/ConorLeckey>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.cannonhighlight;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.util.ArrayList;

import static net.runelite.api.ObjectID.CANNON_BASE;
import static net.runelite.api.ObjectID.CANNON_BASE_43029;

@Slf4j
@PluginDescriptor(
	name = "Cannon Highlighter"
)
public class CannonHighlighterPlugin extends Plugin
{
	@Getter(AccessLevel.PACKAGE)
	private boolean cannonPlaced = false;

	@Getter(AccessLevel.PACKAGE)
	private WorldPoint cannonPosition = null;

	@Inject
	private Client client;

	@Inject
	private CannonHighlighterConfig config;

	@Inject
	private CannonHighlighterOverlay npcOverlay;

	@Inject
	private OverlayManager overlayManager;

	ArrayList<LocalPoint> cannonDoubleHitSpots = new ArrayList<>();
	ArrayList<LocalPoint> cannonNeverHitSpots = new ArrayList<>();

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(npcOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(npcOverlay);
		cannonPlaced = false;
		cannonPosition = null;
		cannonNeverHitSpots.clear();
		cannonDoubleHitSpots.clear();
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject gameObject = event.getGameObject();
		int placedObjectId = gameObject.getId();

		Player localPlayer = client.getLocalPlayer();
		if ((placedObjectId == CANNON_BASE || placedObjectId == CANNON_BASE_43029) && !cannonPlaced)
		{
			if (localPlayer.getWorldLocation().distanceTo(gameObject.getWorldLocation()) <= 2
					&& localPlayer.getAnimation() == AnimationID.BURYING_BONES)
			{
				cannonPosition = gameObject.getWorldLocation();
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.SPAM && event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		if (event.getMessage().equals("You add the furnace."))
		{
			cannonPlaced = true;
		}

		if (event.getMessage().contains("You pick up the cannon")
				|| event.getMessage().contains("Your cannon has decayed. Speak to Nulodion to get a new one!"))
		{
			cannonPlaced = false;
			cannonPosition = null;
			cannonNeverHitSpots.clear();
			cannonDoubleHitSpots.clear();
		}
	}

	@Provides
	CannonHighlighterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CannonHighlighterConfig.class);
	}
}
