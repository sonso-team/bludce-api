package org.sonso.bludceapi.util

object MailGenerator {
    fun passwordMailTemplate(password: String) = """
                <!DOCTYPE html>
                <html lang="en">
                  <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <title>Mail</title>
                  </head>
                  <body style="padding: 30px 0; background: linear-gradient(60deg, #daf5ff, #ecdef8); background-repeat: no-repeat; text-align: center; font-family: Arial, Helvetica, sans-serif;">
                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" width="100%">
                      <tr>
                        <td style="text-align: center; padding-bottom: 20px;">
                          <img src="https://i.postimg.cc/63fKThwW/image.png" alt="logo" style="max-width: 100%; height: auto;" />
                        </td>
                      </tr>
                      <tr>
                        <td>
                          <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" style="max-width: 600px; margin: 0 auto; background: #fff; border-radius: 15px;">
                            <tr>
                              <td style="padding: 20px; text-align: center;">
                                <h2 style="font-weight: 1000; font-size: 28px; margin-bottom: 10px; color: #1c0fb1; text-align: center;">Одноразовый пароль для входа</h2>
                                <p style="font-size: 16px; color: #2d164a; text-align: center;">Ваш одноразовый пароль для входа</p>
                                <div style="padding: 15px 25px; background-color: #1c0fb1; color: #fff; font-size: 22px; border-radius: 10px; margin-bottom: 10px; width: 220px; margin: 10px auto; text-align: center;">
                                  <p style="font-weight: 1000; font-size: 28px; margin: 0;">$password</p>
                                </div>
                                <p style="font-size: 16px; color: #2d164a; text-align: center;">Если вы не запрашивали письмо с паролем - пожалуйста проигнорируйте его</p>
                                <footer style="font-size: 16px; text-align: center; margin-top: 20px;">
                                  <p>&copy; 2025 Такая-то Такая-то. Все права защищены.</p>
                                </footer>
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
