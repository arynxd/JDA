/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.api.interactions.components;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Interaction on a {@link Button} component.
 *
 * @see net.dv8tion.jda.api.events.interaction.ButtonClickEvent
 */
public interface ButtonInteraction extends ComponentInteraction
{
    @Nullable
    @Override
    default Button getComponent()
    {
        return getButton();
    }

    /**
     * The {@link Button} this interaction belongs to.
     * <br>This is null for ephemeral messages!
     *
     * @return The {@link Button}
     *
     * @see    #getComponentId()
     */
    @Nullable
    Button getButton();

    /**
     * Update the button with a new button instance.
     * <br>This only works for non-ephemeral messages where {@link #getMessage()} is available!
     *
     * <p>If this interaction is already acknowledged this will use {@link #getHook()}
     * and otherwise {@link #editComponents(Collection)} directly to acknowledge the interaction.
     *
     * @param  newButton
     *         The new button to use, or null to remove this button from the message entirely
     *
     * @throws IllegalStateException
     *         If this interaction was triggered by a button on an ephemeral message.
     *
     * @return {@link RestAction}
     */
    @Nonnull
    @CheckReturnValue
    default RestAction<Void> editButton(@Nullable Button newButton)
    {
        Message message = getMessage();
        if (message == null)
            throw new IllegalStateException("Cannot update button for ephemeral messages! Discord does not provide enough information to perform the update.");
        List<ActionRow> components = new ArrayList<>(message.getActionRows());
        String id = getComponentId();
        find: for (Iterator<ActionRow> rows = components.iterator(); rows.hasNext();)
        {
            List<Component> row = rows.next().getComponents();
            for (ListIterator<Component> it = row.listIterator(); it.hasNext();)
            {
                Component component = it.next();
                if (id.equals(component.getId()))
                {
                    if (newButton == null)
                        it.remove();
                    else
                        it.set(newButton);
                    if (row.isEmpty())
                        rows.remove();
                    break find;
                }
            }
        }

        if (isAcknowledged())
            return getHook().editMessageComponentsById(message.getId(), components).map(it -> null);
        else
            return editComponents(components).map(it -> null);
    }
}