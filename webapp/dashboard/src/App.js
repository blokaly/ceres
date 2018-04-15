import React, { Component } from 'react';
import {
   BrowserRouter as Router,
   Route,
   Link,
   Switch
} from 'react-router-dom'

import './App.css';
import Status from "./components/Status"
import { default as GrommetApp } from 'grommet/components/App'
import Header from 'grommet/components/Header';
import Title from 'grommet/components/Title';
import Split from 'grommet/components/Split';
import Sidebar from 'grommet/components/Sidebar';
import Menu from 'grommet/components/Menu';
import Anchor from 'grommet/components/Anchor';
import Box from 'grommet/components/Box';




const Home = () => (
   <div>
      <h2>Home</h2>
   </div>
)

const About = () => (
   <div>
      <h2>About</h2>
   </div>
)

const Topic = ({ match }) => (
   <div>
      <h3>{match.params.topicId}</h3>
   </div>
)

const Topics = ({ match }) => (
   <div>
      <h2>Topics</h2>
      <ul>
         <li>
            <Link to={`${match.url}/rendering`}>
               Rendering with React
            </Link>
         </li>
         <li>
            <Link to={`${match.url}/components`}>
               Components
            </Link>
         </li>
         <li>
            <Link to={`${match.url}/props-v-state`}>
               Props v. State
            </Link>
         </li>
      </ul>

      <Route path={`${match.path}/:topicId`} component={Topic}/>
      <Route exact path={match.path} render={() => (
         <h3>Please select a topic.</h3>
      )}/>
   </div>
)

class App extends Component {
  render() {
    return (
       <Router>
             <GrommetApp centered={false}>
                <Split priority="right" flex="right">
                   <Sidebar colorIndex="neutral-1" fixed={false}>
                      <Header size="large" justify="between" pad={{horizontal: 'medium'}}>
                         <Title>Ceres Dashboard</Title>
                      </Header>
                      <Menu fill={true} primary={true}>
                         <Anchor key="home" path="/" label="Home" />
                         <Anchor key="about" path="/about" label="About" />
                         <Anchor key="topics" path="/topics" label="Topics" />
                         <Anchor key="status" path="/status" label="Status" />
                      </Menu>
                   </Sidebar>
                   <Switch>
                      <Route exact path="/" component={Home}/>
                      <Route path="/about" component={About}/>
                      <Route path="/topics" component={Topics}/>
                      <Route path="/status" component={Status}/>
                   </Switch>
                </Split>
             </GrommetApp>


       </Router>
    );
  }
}
export default App;
