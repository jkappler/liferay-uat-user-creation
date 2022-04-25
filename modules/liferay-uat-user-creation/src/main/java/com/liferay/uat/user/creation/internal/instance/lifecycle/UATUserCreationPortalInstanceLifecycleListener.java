/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.uat.user.creation.internal.instance.lifecycle;

import com.liferay.portal.instance.lifecycle.BasePortalInstanceLifecycleListener;
import com.liferay.portal.instance.lifecycle.PortalInstanceLifecycleListener;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.role.RoleConstants;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalService;

import java.util.Locale;

import org.json.JSONObject;
import org.json.JSONTokener;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Jürgen Kappler
 */
@Component(immediate = true, service = PortalInstanceLifecycleListener.class)
public class UATUserCreationPortalInstanceLifecycleListener
	extends BasePortalInstanceLifecycleListener {

	@Override
	public void portalInstanceRegistered(Company company) throws Exception {
		JSONObject userJSONObject = new JSONObject(
			new JSONTokener(
				UATUserCreationPortalInstanceLifecycleListener.class.
					getResourceAsStream("dependencies/user.json")));

		User user = _userLocalService.fetchUserByScreenName(
			company.getCompanyId(), userJSONObject.getString("screenName"));

		if (user != null) {
			return;
		}

		Role role = _roleLocalService.getRole(
			company.getCompanyId(), RoleConstants.ADMINISTRATOR);

		long[] roles = {role.getRoleId()};

		User defaultUser = _userLocalService.getDefaultUser(
			company.getCompanyId());

		user = _userLocalService.addUser(
			defaultUser.getUserId(), company.getCompanyId(), false,
			userJSONObject.getString("password"),
			userJSONObject.getString("password"), false,
			userJSONObject.getString("screenName"),
			userJSONObject.getString("email"), Locale.US,
			userJSONObject.getString("firstName"), null,
			userJSONObject.getString("lastName"), 0, 0, true, 1, 1, 1970, null,
			null, null, roles, null, false,
			ServiceContextThreadLocal.getServiceContext());

		user.setAgreedToTermsOfUse(true);
		user.setReminderQueryAnswer(userJSONObject.getString("reminderQueryAnswer"));
		user.setReminderQueryQuestion(userJSONObject.getString("reminderQueryQuestion"));

		_userLocalService.updateUser(user);
	}

	@Reference
	private RoleLocalService _roleLocalService;

	@Reference
	private UserLocalService _userLocalService;

}