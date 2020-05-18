/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.cuda.datahub.webserver.auth

import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.authz.SimpleAuthorizationInfo
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import tech.cuda.datahub.service.UserService

/**
 * @author Jensen Qi <jinxiu.qi@alu.hit.edu.cn>
 * @since 1.0.0
 */
class ShiroRealm : AuthorizingRealm() {
    override fun doGetAuthenticationInfo(authToken: AuthenticationToken?): SimpleAuthenticationInfo {
        val token = authToken?.credentials.toString()
        if (UserService.verify(token)) {
            return SimpleAuthenticationInfo(token, token, "shiro_realm")
        } else {
            throw AuthenticationException("wrong token")
        }
    }

    override fun doGetAuthorizationInfo(token: PrincipalCollection?): AuthorizationInfo {
        val user = UserService.getUserByToken(token.toString())
        val isRootGroup = user?.groups?.contains(1) ?: false
        return if (isRootGroup) {
            SimpleAuthorizationInfo().also {
                it.roles = setOf("root")
                it.stringPermissions = setOf("root")
            }
        } else {
            SimpleAuthorizationInfo().also {
                it.roles = setOf()
                it.stringPermissions = setOf()
            }
        }
    }

    override fun supports(token: AuthenticationToken?) = token is JwtToken

}