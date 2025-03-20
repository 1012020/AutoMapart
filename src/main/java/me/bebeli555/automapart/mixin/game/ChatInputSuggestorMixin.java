package me.bebeli555.automapart.mixin.game;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import me.bebeli555.automapart.command.Command;
import me.bebeli555.automapart.mods.ClientSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public class ChatInputSuggestorMixin {
    private MinecraftClient mc = MinecraftClient.getInstance();

    @Inject(method = "refresh", at = @At("HEAD"), cancellable = true)
    private void onRefresh(CallbackInfo ci) {
        ChatInputSuggestor self = (ChatInputSuggestor)(Object)this;

        String string = self.textField.getText();
        if (string.startsWith(ClientSettings.prefix.string())) {
            ci.cancel();

            int cursor = self.textField.getCursor();
            if (cursor >= 1 && (self.window == null || !self.completingSuggestions)) {
                String string2 = string.substring(0, cursor);

                List<Suggestion> list = new ArrayList<>();
                for (Command.CommandCompletion completion: Command.getCompletions(string2)) {
                    list.add(new Suggestion(StringRange.between(completion.start(), completion.start() + completion.command().length()), completion.command()));
                }

                CompletableFuture<Suggestions> suggestions = new CompletableFuture<>();
                suggestions.complete(new Suggestions(StringRange.between(0, list.size()), list));

                CommandDispatcher<CommandSource> commandDispatcher = mc.player.networkHandler.getCommandDispatcher();
                if (self.parse == null) {
                    self.parse = commandDispatcher.parse(new StringReader(""), mc.player.networkHandler.getCommandSource());
                }

                self.pendingSuggestions = suggestions;
                self.showCommandSuggestions();

                if (list.get(0).getText().equals("")) {
                    self.setWindowActive(false);
                }
            }
        }
    }
}
