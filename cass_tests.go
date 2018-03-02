package main

import (
	//"encoding/json"
	"github.com/gocql/gocql"
	//. "github.com/onsi/ginkgo"
	//. "github.com/onsi/gomega"
	//"flag"
	"fmt"
	//"strings"
	"time"
)

func createkeyspace() {
	var err error
	var session *gocql.Session
	var cluster *gocql.ClusterConfig
	var clusterAdress = "10.165.0.95"
	var authSuperUser = gocql.PasswordAuthenticator{"cassandra", "kc7uos8buhtzj23qfkfd"} //kc7uos8buhtzj23qfkfd"}
	var keyspaceName = "keyspace5"
	var strategy = SimpleStrategy
	var RF = 3
	var strat = fmt.Sprintf("{ 'class' : '%s', 'replication_factor' : %d }", strategy, RF)
	var write = false

	var createKeySpace = fmt.Sprintf("CREATE KEYSPACE IF NOT EXISTS %s WITH REPLICATION = %s AND DURABLE_WRITES = %t", keyspaceName, strat, write) // essai en dur

	cluster = gocql.NewCluster(clusterAdress)
	cluster.Authenticator = authSuperUser
	cluster.Timeout = 60 * time.Second
	cluster.Keyspace = "system"
	cluster.ProtoVersion = 4
	cluster.ConnectTimeout = 10 * time.Second

	session, err = cluster.CreateSession()
	if err != nil {
		fmt.Println("failed connexion to keyspace system", err)
		return
	} else {
		err = session.Query(createKeySpace).Exec()
		if err != nil {
			fmt.Println("failed creation of keyspace")
			return
		} else {
			fmt.Println("successful creation of keyspace")

			session.Close()
		}

	}
}

func createUser() {
	var err error
	var session *gocql.Session
	var cluster *gocql.ClusterConfig
	var clusterAdress = "10.165.0.95"
	var authSuperUser = gocql.PasswordAuthenticator{"cassandra", "kc7uos8buhtzj23qfkfd"}
	var createUser = "CREATE USER IF NOT EXISTS username + WITH PASSWORD + pwdNewUser + SUPERUSER"
	cluster = gocql.NewCluster(clusterAdress)
	cluster.Authenticator = authSuperUser
	cluster.Timeout = 60 * time.Second
	cluster.Keyspace = "keyspaceName"
	session, err = cluster.CreateSession()
	if err != nil {
		fmt.Println("failed connexion to keyspaceName")
		return
	} else {
		fmt.Println("successful connexion to keyspaceName")
		err = session.Query(createUser).Exec()
		if err != nil {
			fmt.Println("failed creation of user")
			return
		} else {
			fmt.Println("successful creation of user")
		}
	}
}

func main() {
	createkeyspace()
	return
}

/*


var err error
	var session *gocql.Session
	var cluster *gocql.ClusterConfig
	var clusterAdress = "10.165.0.95"
	//var differentiator = "uuid.NewV4().String()" //remove the double quote later
	//var nameNewUser = "newUser" + differentiator
	//var pwdNewUser = "pwd" + differentiator
	var keyspaceName = "keyspaceName" //+ differentiator
	var authNewUser = gocql.PasswordAuthenticator{nameNewUser, pwdNewUser}
	var RF = "3"     	                                                                             //essai en dur
	var replicationStrat = map[string]string{"class": "SimpleStrategy", "replication_factor": RF} //essai en dur
	replicationBytes, err := json.Marshal(replicationStrat)
	var replicationMap = strings.Replace(string(replicationBytes), `"`, `'`, -1)
	var createKeySpace = "CREATE KEYSPACE IF NOT EXISTS  keyspaceName  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 }"
	var createUser = "CREATE USER IF NOT EXISTS" + nameNewUser + "WITH PASSWORD" + " pwdNewUser" + " SUPERUSER" //to modify
	var dropKeySpace = "DROP KEYSPACE IF EXISTS " + keyspaceName
	BeforeEach(func() {
		fmt.Println("seeds:", config.Seeds)
		fmt.Println("servers:", config.Servers)
		fmt.Println("replstrat :", replicationStrat)
		fmt.Print("createKeyspace: ", createKeySpace)
		By("finding the cluster")
		cluster = gocql.NewCluster(clusterAdress)


} */
