package com.dbkynd.DBBungeeBot.permissions;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LuckPermissions {

    LuckPerms luckApi;

    public boolean hasPermission(String uuid, String permission) {
        // Connect to the LuckPerms API
        try {
            luckApi = LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        if (luckApi == null) return false;

        // Get the LuckPerms User
        User user = getUser(UUID.fromString(uuid));
        if (user == null) return false;

        // Get the permission data cache for the user
        CachedPermissionData permissionData = user.getCachedData().getPermissionData(QueryOptions.nonContextual());
        // Check the permission
        Tristate checkResult = permissionData.checkPermission(permission);
        return checkResult.asBoolean();
    }

    public User getUser(UUID uuid) {
        UserManager userManager = luckApi.getUserManager();
        CompletableFuture<User> userFuture = userManager.loadUser(uuid);

        return userFuture.join();
    }
}
