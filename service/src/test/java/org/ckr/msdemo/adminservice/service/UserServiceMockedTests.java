package org.ckr.msdemo.adminservice.service;

import com.google.common.base.Strings;
import mockit.Expectations;
import mockit.Verifications;
import org.ckr.msdemo.adminservice.TestUtil;
import org.ckr.msdemo.adminservice.entity.User;
import org.ckr.msdemo.adminservice.valueobject.UserServiceForm;
import org.ckr.msdemo.exception.ApplicationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by Administrator on 2017/10/15.
 */
public class UserServiceMockedTests extends UserServiceMockedTestsBase{



    @Test
    public void testCreateUser_successfully() {

        UserServiceForm form = new UserServiceForm();

        form.setUserName("userName");
        form.setUserDescription("userDescrption");
        form.setLocked(Boolean.TRUE);
        form.setPassword("password");

        new Expectations() {{
            userRepository.findByUserName(anyString);
            result = null;

        }};

        this.userService.createUser(form);

        new Verifications() {{
            User user;
            userRepository.save(user = withCapture());
            times = 1;

            assertThat(user.getUserName()).isEqualTo(form.getUserName());
            assertThat(user.getUserDescription()).isEqualTo(form.getUserDescription());
            assertThat(user.getLocked()).isEqualTo(Boolean.FALSE);
            assertThat(user.getPassword()).isNotNull();
        }};
    }

    private void doTestCreateUserValidation(String userName,
                                            String userDescription,
                                            User previosuUser,
                                            String messageCode,
                                            String message) {

        UserServiceForm form = new UserServiceForm();

        form.setUserName(userName);
        form.setUserDescription(userDescription);


        if(!Strings.isNullOrEmpty(userName)) {
            new Expectations() {{
                userRepository.findByUserName(userName);
                result = previosuUser;
            }};
        }

        try {
            this.userService.createUser(form);
        } catch (ApplicationException ae) {
            TestUtil.checkErrorMsg(ae, messageCode, message);
            return;
        }

        fail("ApplicationException is expected.");
    }

    @Test
    public void testCreateUser_userNameIsEmpty() {

        doTestCreateUserValidation("",
                                   "desc",
                                   null,
                                   "security.maintain_user.user_name_empty",
                                   "User name cannot be empty.");

    }

    @Test
    public void testCreateUser_userDescriptionIsEmpty() {

        doTestCreateUserValidation("userName",
                "",
                null,
                "security.maintain_user.user_desc_empty",
                "User description cannot be empty.");

    }

    @Test
    public void testCreateUser_duplicatedUser() {

        doTestCreateUserValidation("userName",
                "",
                new User(),
                "security.maintain_user.duplicated_user",
                "The user name is duplicated with an existing one.");

    }


    /**
     * Test the validation logic for createUser method.
     */
    @RunWith(Parameterized.class)
    public static class CreateUserValidationMockedTests extends UserServiceMockedTestsBase {

        private String userName;
        private String userDescription;
        private User previousUser;
        private String messageCode;
        private String message;

        public CreateUserValidationMockedTests(String userName,
                                               String userDescription,
                                               User previousUser,
                                               String messageCode,
                                               String message) {
            this.userName = userName;
            this.userDescription = userDescription;
            this.previousUser = previousUser;
            this.messageCode = messageCode;
            this.message = message;
        }

        @Parameterized.Parameters
        public static Collection testParams() {
            return Arrays.asList(new Object[][] {
                //user name    user description   previous user   exception message code
                //exception message
                { ""         , "userDescription", null      ,     "security.maintain_user.user_name_empty",
                 "User name cannot be empty."},
                { "userName" , ""               , null      ,     "security.maintain_user.user_desc_empty",
                 "User description cannot be empty."},
                { "userName" , "userDesc"       , new User(),     "security.maintain_user.duplicated_user",
                 "The user name is duplicated with an existing one."}
            });
        }

        @Test
        public void testCreateUser_withErrorMessage() {

            UserServiceForm form = new UserServiceForm();

            form.setUserName(this.userName);
            form.setUserDescription(this.userDescription);

            final CreateUserValidationMockedTests toThis = this;

            if(!Strings.isNullOrEmpty(this.userName)) {
                new Expectations() {{
                    userRepository.findByUserName(toThis.userName);
                    result = toThis.previousUser;
                }};
            }

            try {
                this.userService.createUser(form);
            } catch (ApplicationException ae) {
                TestUtil.checkErrorMsg(ae, messageCode, message);

                return;
            }

            fail("ApplicationException is expected.");

        }
    }


}
