package org.sonso.bludceapi.util

object MailGenerator {
    fun passwordMailTemplate(password: String) = """
                <!DOCTYPE html>
                <html lang="en">
                  <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <title>Verification mail</title>
                  </head>
                  <body style="padding: 30px 0; text-align: center; font-family: Arial, Helvetica, sans-serif; ">
                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" width="100%">
                      <tr>
                        <!-- ВНИМАНИЕ: фон теперь здесь! -->
                        <td align="center">
                          <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" style="max-width: 500px; width: 100%; background-image: url('https://bobr.uwu-devcrew.ru/download/62c51756-c338-4d10-a823-73db5e3f5945');  background-repeat: no-repeat; background-size: cover; background-position: top center; ">
                            <tr align="center" >
                              <td style="background-color: rgba(0, 0, 0, 0.4)">
                                <img style="margin-top: 60px; max-width: 90%; height: auto;" src="https://bobr.uwu-devcrew.ru/download/72dcefe7-6ee8-40d6-bfee-d1b48cb7ffc6" />
                              </td>
                            </tr>
                            <tr>
                              <td style="background-color: rgba(0, 0, 0, 0.4)">
                                <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" style="max-width: 400px; width: 80%; margin: 0 auto; margin-top: 20px; background: rgba(0, 0, 0, 0.8); border-radius: 15px;">
                                  <tr>
                                    <td style="padding: 30px 20px; text-align: center;">
                                      <img style="max-width: 60%; height: auto;" src="https://bobr.uwu-devcrew.ru/download/6521ec4a-e10c-4123-84bb-1c2e84df455d" />
                                      <p style="font-weight: 500; font-size: 32px; margin: 0; margin-top: 20px; color: #fff;">$password</p>
                                      <p style="display: none;"> - вот он!</p>
                                      <img src="https://bobr.uwu-devcrew.ru/download/290649ef-9182-4951-b574-78e0bee8f5e7" style="margin-top: 20px; max-width: 90%; height: auto; color: #fff;" alt="ваш одноразовый пароль для входа в приложение" />
                                      <img src="https://bobr.uwu-devcrew.ru/download/b8a38e3d-e6e7-44bb-9aa2-8756aad54932" style="max-width: 70%; height: auto; margin-top: 40px;" />
                                    </td>
                                  </tr>
                                </table>
                              </td>
                            </tr>
                            <tr align="center">
                              <td style="background-color: rgba(0, 0, 0, 0.4)">
                                <img src="https://bobr.uwu-devcrew.ru/download/990e8470-939d-4633-a46e-b7ec163e31ba" style="max-width: 90%; height: auto; margin-top: 20px;" />
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
            """
}
