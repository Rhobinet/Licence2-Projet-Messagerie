package com.example.demo;

import java.sql.*;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Cette classe est la classe principale de la messagerie, elle permet l'affichage des messages, des personnes avec qui ont a eu contact et l'envoie de nouveaux messages.
 *
 * @author Kéran Carvalhais
 * @version 6
 */
public class MessagerieController {

    @FXML
    public TextArea taMessage;
    @FXML
    public AnchorPane anchorPane;
    @FXML
    private VBox vb_UserButton;
    @FXML
    private VBox vb_UserMessages;
    @FXML
    public Button bt_Envoyer;
    @FXML
    public ScrollPane sp_UserMessages;
    @FXML
    public TextField tf_Ajout_User;
    @FXML
    public Label inexistant;

    static Statement stmt;
    static Connection connection;
    static List<String> liste_User = new ArrayList<String>();
    static List<String> liste_User_Sender = new ArrayList<String>();
    static List<String> liste_All_Messages = new ArrayList<String>();
    static List<String> liste_All_Date = new ArrayList<String>();
    static Button bt_NomUser = new Button();

    /**
     * La méthode connexion_BDD permet de se connecter à la base de donnée.
     */
    public static void connexion_BDD() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/messagerie", "root", "root");
            stmt = connection.createStatement();
        } catch (SQLException e) {
            System.err.println(e);
        }
        return;
    }

    /**
     * get_Count permet de compter le nombre de fois où quelque chose apparaît, par rapport à une requête, dans une table de la base de donnée.
     *
     * @param query La requête que l'on veut compter.
     * @return La valeur du nombre de fois où la requête àeu un résultat.
     * @throws SQLException
     */
    public static int get_Count(String query) throws SQLException {
        int count;
        ResultSet nbr_Receiver = stmt.executeQuery(query);
        nbr_Receiver.next();
        count = nbr_Receiver.getInt("rowcount");
        return count;
    }

    /**
     * liste_User est la méthode qui va regrouper tous les utilisateurs à qui l'utilisateur connecté a envoyé des messages ou en a reçu.
     * La méthode effectue d'abord la requête dans la BDD, puis récupère chaque pseudo qui on reçu des messages de l'utilisateur connecté et l'ajoute à la liste_User qui sera utile dans d'autres méthodes.
     * Et ensuite la méthode récupère aussi les pseudos des personnes qui ont envoyés un message à celui qui est connecté, pour aussi les rajouter dans la liste_User.
     *
     * @return La liste des utilisateurs qui ont reçus ou envoyés des messages à l'utilisateur présentement connecté.
     * @throws SQLException
     */
    public static List<String> liste_User() throws SQLException {
        //prend tous les pseudos des personnes qui ont reçus un message de la part de l'utilisateur connecté
        String query = "select distinct pseudo_User_Receiver from messagerie.messages where pseudo_User_Sender = ?;";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, "xxxxxx");
        ResultSet pseudo_Receiver = preparedStatement.executeQuery();

        //ajout de chaque pseudo dans la liste "liste_User"
        while (pseudo_Receiver.next()) {
            String pseudo = pseudo_Receiver.getString(1); //récupère le pseudo dans la BDD
            liste_User.add(pseudo);
        }

        //requête qui va nous permettre d'avoir le nombre d'utilisateur qui ont envoyés des messages à l'utilisateur connecté
        String query_Count = "select count(pseudo_User_Receiver) AS rowcount from messagerie.messages where pseudo_User_Receiver = ?;";
        PreparedStatement preparedStatement1 = connection.prepareStatement(query_Count);
        preparedStatement1.setString(1, "xxxxxx");
        ResultSet c = preparedStatement1.executeQuery();
        c.next();
        int count = c.getInt("rowcount");

        if (count != 0) { //s'il y a des utilisateurs qui ont envoyés des messages
            //récupère les pseudos de ces utilisateurs
            String query2 = "select distinct pseudo_User_Sender from messagerie.messages where pseudo_User_Receiver = ?;";
            PreparedStatement preparedStatement2 = connection.prepareStatement(query2);
            preparedStatement2.setString(1, "xxxxxx");
            ResultSet pS2 = preparedStatement2.executeQuery();

            while (pS2.next()) {
                String pseudo = pS2.getString(1);
                Boolean b = false;
                for (int i = 0; i < liste_User.size(); i++) {
                    //vérifie que l'on n'ai pas déjà mis le pseudo dans liste (c'est le cas si l'utilisateur connecté à envoyer des messages à l'utilisateur que l'on vérifie)
                    if (liste_User.get(i).equals(pseudo))
                        b = true;
                }
                if (!b)
                    liste_User.add(pseudo);
                if(liste_User.contains(null))
                    for(int i = 0; i < liste_User.size(); i++){
                        if(null == liste_User.get(i))
                            liste_User.remove(i);
                    }
            }
        }
        return liste_User;
    }

    /**
     * Cette méthode va récupérer les messages envoyés entre 2 utilisateurs (celui connecté et l'utilisateur présent au niveau d'un bouton et qui est selectionné).
     * La méthode vérifie d'abord si les messages que l'on veut récupérer ne soit pas les messages que l'on s'est envoyé à soi-même,
     * si c'est le cas on récupère que les messages envoyés dans la BDD, sinon on récupère les messages envoyés et les messages reçus.
     * Pour chaque message on fait une liste de messages, une liste de l'utilisateur qui a envoyé le message et une liste de l'heure du message.
     *
     * @param text_Bt Le nom de l'utilisateur auquel on veut récuperer les messages.
     * @throws SQLException
     */
    public void getListe_All_Messages(String text_Bt) throws SQLException {

        Boolean b = false;
        if (text_Bt.equals("xxxxxx")) //permet de vérifier que l'utilisateur choisit ne soit pas celui connecté
            b = true;

        if (b) { //l'utilisateur choisit est celui qui est connecté
            String query = "select messages, pseudo_User_Sender, pseudo_User_Receiver, date_Heure from messagerie.messages where pseudo_User_Sender = ? and pseudo_User_Receiver = ? order by date_Heure;";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, "xxxxxx");
            preparedStatement.setString(2, text_Bt);
            ResultSet rs = preparedStatement.executeQuery();

            //clear des listes pour éviter la redondance
            liste_User_Sender.clear();
            liste_All_Messages.clear();
            liste_All_Date.clear();

            //s'il n'y a pas encore eu de messages entre les 2 utilisateurs on fait un return
            if (!rs.isBeforeFirst())
                return;
            //ajoute dans chaque liste l'élément correspondant
            while (rs.next()) {
                String me = rs.getString(1); //le message
                String user_Sender = rs.getString(2); //l'utilisateur qui a envoyé le message
                String date = rs.getString(4); //la date et l'heure à laquelle a été écrit le message
                //ajout dans les listes
                liste_All_Messages.add(me);
                liste_User_Sender.add(user_Sender);
                liste_All_Date.add(date);
            }

        } else { //même fonctionnement qu'au dessus mais cette fois l'utilisateur choisit n'est pas celui connecté, donc on récupère les messages reçus et envoyés
            String query = "select * from ((select messages, pseudo_User_Sender, pseudo_User_Receiver, date_Heure from messagerie.messages where pseudo_User_Sender = ? and pseudo_User_Receiver = ?) union all (SELECT messages, pseudo_User_Sender, pseudo_User_Receiver, date_Heure FROM messagerie.messages WHERE pseudo_User_Sender = ? and pseudo_User_Receiver = ?)) as a order by a.date_Heure;";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, "xxxxxx");
            preparedStatement.setString(2, text_Bt);
            preparedStatement.setString(3, text_Bt);
            preparedStatement.setString(4, "xxxxxx");
            ResultSet rs = preparedStatement.executeQuery();

            liste_User_Sender.clear();
            liste_All_Messages.clear();
            liste_All_Date.clear();

            if (!rs.isBeforeFirst())
                return;
            while (rs.next()) {
                String me = rs.getString(1);
                String user_Sender = rs.getString(2);
                String date = rs.getString(4);
                liste_All_Messages.add(me);
                liste_User_Sender.add(user_Sender);
                liste_All_Date.add(date);
            }
        }
    }

    /**
     * aff_Messages affiche tous les messages d'une conversation en particulier.
     * Lorsqu'un bouton est cliqué, on commence par clear liste_All_Messages et la vbox "vb_UserMessages" pour éviter tout problème de redondance,
     * ensuite on affiche taMessage et bt_Envoyer qui permettent d'envoyer des messages. Et enfin on va appeler la méthode getListe_All_Messages,
     * qui retourne les listes de tous les messages, de quel utilisateur et à quel heure, pour les mettres dans des labels qui seront ajoutés à la vbox "vb_UserMessages".
     *
     * @param bt Le bouton sur lequel l'utilisateur a appuyé.
     */
    public void aff_Messages(Button bt) {
        bt.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bt_NomUser = bt; //sauvegarde du bouton appuyé pour pouvoir le réutiliser plus bas
                liste_All_Messages.clear(); //clear de la liste

                vb_UserMessages.getChildren().clear(); //clear de la vbox
                vb_UserMessages.setSpacing(2.0); //avoir un espacement de 2.0 entre chaque élément de la vbox

                taMessage.setVisible(true); //l'affichage du textArea permettant d'envoyer un message
                bt_Envoyer.setVisible(true); //l'affichage du bouton permettant d'envoyer un message
                inexistant.setVisible(false); //rend le label invisible s'il a du être affiché

                try {
                    //appel de la méthode permettant d'avoir les messages, l'heure et qui les a envoyés à qui
                    getListe_All_Messages(bt_NomUser.getText());
                } catch (SQLException e) {
                    System.err.println(e);
                }

                //récupération de chaque élément des 3 listes utilisées
                for (int i = 0; i < liste_All_Messages.size(); i++) {
                    String message = liste_All_Messages.get(i);
                    String user_Sender = liste_User_Sender.get(i);
                    String heure = liste_All_Date.get(i);

                    Label msg = new Label(user_Sender + " : " + message);

                    if (user_Sender.equals(bt_NomUser.getText())) //vérifie si le message est un message envoyé ou reçu pour avoir un style d'affichage différent dans le css
                        msg.setId("msg_UserReceiver"); //permet de controller le style dans la page css
                    else
                        msg.setId("msg_UserSender");

                    Label hr = new Label(heure);
                    hr.setId("heure");

                    vb_UserMessages.getChildren().addAll(msg, hr); //ajout des messages et de l'heure dans la vbox
                }

                sp_UserMessages.vvalueProperty().bind(vb_UserMessages.heightProperty()); //met la barre de déffilement tout en bas, permettant l'affichage des messages les plus récents.
            }
        });
    }

    /**
     * Permet d'ajouter tous les utilisateurs qui ont eu une intéraction avec l'utilisateur actuellement connecté dans des boutons.
     * La méthode clear la vbox "vb_UserButton" pour éviter la redondance de bouton dans l'affichage, puis on appel liste_User(),
     * qui va nous permettre d'avoir les utilisateurs avec qui on a eu une conversation. On va créer un bouton pour chaque utilisateur
     * dans la liste liste_User. Et enfin on appel aff_Messages qui permet d'afficher les messages, si un bouton est appuyé.
     *
     * @throws SQLException
     */
    @FXML
    public void add_UserButton() throws SQLException {
        vb_UserButton.getChildren().clear();
        vb_UserButton.setSpacing(2);
        liste_User();
        //ajout d'un bouton pour chaque utilisateur dans la liste "liste_User"
        for (int i = 0; i < liste_User.size(); i++) {
            Button a = new Button(liste_User.get(i));
            a.setId("bt_NomUser");
            vb_UserButton.getChildren().add(a);
            aff_Messages(a);
        }
    }

    /**
     * envoie_Message est la méthode qui ajoute à la base de données, un message entré par l'utilisateur connecté.
     * Cette méthode est reliée dans la page fxml au bouton "bt_envoyer", et lorsqu'un clique dessus est détecté,
     * le message mis dans le textArea "taMessage" est ajouté à la base de données. Puis on réaffiche dans la vbox "vb_UserMessages"
     * tous les messages, qu'il y a dans la BDD, lié aux utilisateurs.
     *
     * @throws SQLException
     */
    @FXML
    public void envoie_Message() throws SQLException {
        vb_UserMessages.getChildren().clear();
        String enterMessage = taMessage.getText(); //récupère le text dans le textArea "taMessage"
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); //définit le format de l'heure
        LocalDateTime now = LocalDateTime.now(); //récupère l'heure actuelle

        String query = "insert into messagerie.messages (messages, pseudo_User_Sender, pseudo_User_Receiver, date_Heure)"
                + "values ( ?, ?, ?, ?);";

        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, enterMessage);
        preparedStatement.setString(2, "xxxxxx");
        preparedStatement.setString(3, bt_NomUser.getText());
        preparedStatement.setString(4, dtf.format(now));
        preparedStatement.executeUpdate();

        getListe_All_Messages(bt_NomUser.getText());

        //même fonctionnement que la méthode aff_Messages
        for (int i = 0; i < liste_All_Messages.size(); i++) {
            String message = liste_All_Messages.get(i);
            String user_Sender = liste_User_Sender.get(i);
            String heure = liste_All_Date.get(i);

            Label msg = new Label(user_Sender + " : " + message);

            if (user_Sender.equals(bt_NomUser.getText()))
                msg.setId("msg_UserReceiver");
            else
                msg.setId("msg_UserSender");

            Label hr = new Label(heure);
            hr.setId("heure");

            vb_UserMessages.getChildren().addAll(msg, hr);
        }
        taMessage.clear();
    }

    /**
     * envoie_Message_Enter est la méthode qui ajoute à la base de données, un message entré par l'utilisateur connecté.
     * Cette méthode est reliée dans la page fxml à la textArea "taMessage", et lorsque la touche ENTREE est pressé,
     * le message mis dans le textArea "taMessage" est ajouté à la base de données. Puis on réaffiche dans la vbox "vb_UserMessages"
     * tous les messages, qu'il y a dans la BDD, lié aux utilisateurs.
     *
     * @param keyEvent une touche sur laquelle un utilisateur à appuyer
     */
    @FXML
    public void envoie_Message_Enter(KeyEvent keyEvent) throws SQLException {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) { //vérifie si la touche appuyée, est la touche ENTREE
            envoie_Message();
        }
    }

    /**
     * ajout_User permet d'ajouter un bouton avec un utilisateur, qui est la peronne à qui l'on veut envoyer un message.
     * Cette méthode récupère le texte dans le textField "tf_AjoutUser", lorsque le bouton "ajout_User" est appuyé (grâce à la page fxml),
     * appel la méthode get_Count, pour vérifier que l'utilisateur souhaité est belle et bien dans la BDD. Si l'utilisateur existe,
     * on vérifie alors qu'il ne soit pas déjà dans la liste_User, correspondant aux utilisateurs à qui l'on a envoyé un message.
     * Si toutes les conditions sont vérifiées, alors ajoute un bouton avec le pseudo dans la vbox "vb_UserButton".
     *
     * @throws SQLException
     */
    @FXML
    public void ajout_User() throws SQLException {
        //récupère le texte entré (donc le pseudo de l'utilisateur que l'on veut ajouter)
        String user = tf_Ajout_User.getText();
        //permettra de vérifier l'existence du pseudo dans la BDD
        String query_Count = "select count(pseudo) AS rowcount from messagerie.utilisateur where pseudo = \"" + user + "\";";

        liste_User.clear();
        liste_User();
        int count = get_Count(query_Count);
        Boolean exist = false;

        for (int i = 0; i < liste_User.size(); i++) {
            if (user.equalsIgnoreCase(liste_User.get(i))) //permet de vérifier si l'utilisateur et déjà dans la liste
                exist = true;
        }

        if (count == 0) { //si le comptage a donné 0 alors l'utilisateur n'est pas dans la BDD
            inexistant.setText("Utilisateur inexistant");
            inexistant.setVisible(true); //affiche le label inexistant
            return;

        } else if (exist) { //si le pseudo est déjà dans la liste "liste_user"
            inexistant.setText("Déjà ajouté");
            inexistant.setVisible(true);
            return;

        } else {
            //ajout du pseudo dans la liste "liste_User"
            liste_User.clear();
            String query = "select pseudo from messagerie.utilisateur where pseudo = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, user);
            ResultSet pseudo = preparedStatement.executeQuery();
            pseudo.next();
            liste_User.add(pseudo.getString(1));
            add_UserButton();
        }

    }

    /**
     * ajout_User permet d'ajouter un bouton avec un utilisateur, qui est la peronne à qui l'on veut envoyer un message.
     * Cette méthode récupère le texte dans le textField "tf_AjoutUser", lorsque la touche ENTREE est pressée (grâce à la page fxml),
     * appel la méthode get_Count, pour vérifier que l'utilisateur souhaité est belle et bien dans la BDD. Si l'utilisateur existe,
     * on vérifie alors qu'il ne soit pas déjà dans la liste_User, correspondant aux utilisateurs à qui l'on a envoyé un message.
     * Si toutes les conditions sont vérifiées, alors ajoute un bouton avec le pseudo dans la vbox "vb_UserButton".
     *
     * @param keyEvent une touche sur laquelle un utilisateur à appuyer
     * @throws SQLException
     */
    @FXML
    public void ajout_User_Enter(KeyEvent keyEvent) throws SQLException {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) { //vérifie si la touche appuyé est ENTREE
            ajout_User();
        }
    }

    /**
     * Méthode qui fait plusieurs choses dès le lancement de l'application.
     * La méthode appel connexion_BDD(), pour se connecter à la base de données pour le reste de l'utilisation de la messagerie,
     * add_UserButton elle va ajouter les boutons avec qui l'utilisateur a été en contact,
     * puis on rend invisible certains éléments ne s'affichant que dans des conditions précises.
     *
     * @throws SQLException erreur sql
     */
    public void initialize() throws SQLException {
        connexion_BDD();
        liste_User.clear();
        vb_UserButton.getChildren().clear();
        add_UserButton();
        inexistant.setVisible(false);
        taMessage.setVisible(false);
        bt_Envoyer.setVisible(false);
    }
}