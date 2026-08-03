package io.github.seasonalsmp.seasonalsmp.listener;

import io.github.seasonalsmp.seasonalsmp.SeasonalSMP;
import io.github.seasonalsmp.seasonalsmp.config.ConfigManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrivateMessagePrivacyListenerTest {

    @Mock
    SeasonalSMP plugin;

    @Mock
    ConfigManager configManager;

    @Test
    void isPrivateMessageCommand_detectsAllAliases() {
        PrivateMessagePrivacyListener listener = new PrivateMessagePrivacyListener(plugin);
        when(plugin.getConfigManager()).thenReturn(configManager);
        when(configManager.getBoolean("privacy.hide-private-messages", true)).thenReturn(true);

        assertTrue(invokeIsPrivateMessageCommand(listener, "/msg Bob hello"));
        assertTrue(invokeIsPrivateMessageCommand(listener, "/tell Bob hello"));
        assertTrue(invokeIsPrivateMessageCommand(listener, "/w Bob hello"));
        assertTrue(invokeIsPrivateMessageCommand(listener, "/pm Bob hello"));
        assertTrue(invokeIsPrivateMessageCommand(listener, "/whisper Bob hello"));
        assertTrue(invokeIsPrivateMessageCommand(listener, "/reply hello"));
        assertTrue(invokeIsPrivateMessageCommand(listener, "/r hello"));
        assertFalse(invokeIsPrivateMessageCommand(listener, "/season start"));
        assertFalse(invokeIsPrivateMessageCommand(listener, "Hello world"));
    }

    private boolean invokeIsPrivateMessageCommand(PrivateMessagePrivacyListener listener, String input) {
        try {
            java.lang.reflect.Method method = PrivateMessagePrivacyListener.class.getDeclaredMethod("isPrivateMessageCommand", String.class);
            method.setAccessible(true);
            return (boolean) method.invoke(listener, input.toLowerCase(java.util.Locale.ROOT));
        } catch (Exception e) {
            fail("Reflection failed", e);
            return false;
        }
    }
}
