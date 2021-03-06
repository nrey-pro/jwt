/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enumeration for a login state.
 * <p>
 * 
 * @see Login#getState()
 */
public enum LoginState {
	/**
	 * No user is currently identified.
	 */
	LoggedOut,
	/**
	 * The identified user was refused to login.
	 * <p>
	 * This is caused by for example {@link User#getStatus() User#getStatus()}
	 * returning {@link User.Status#Disabled}, or if email verification is
	 * required but the email hasn&apos;t been verified yet.
	 */
	DisabledLogin,
	/**
	 * A user is weakly authenticated.
	 * <p>
	 * The authentication method was weak, typically this means that a secondary
	 * authentication system was used (e.g. an authentication cookie) instead of
	 * a primary mechanism (like a password).
	 * <p>
	 * You may want to allow certain operations, but request to authenticate
	 * fully before more senstive operations.
	 */
	WeakLogin,
	/**
	 * A user is strongly authenticated.
	 */
	StrongLogin;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
