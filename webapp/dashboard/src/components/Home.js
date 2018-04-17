import React, {Component} from 'react'
import Box from 'grommet/components/Box';
import Header from 'grommet/components/Header';
import Title from 'grommet/components/Title';
import Tiles from 'grommet/components/Tiles';
import Card from 'grommet/components/Card';
import Section from 'grommet/components/Section';
import Label from 'grommet/components/Label';
import Anchor from 'grommet/components/Anchor';

export default class Home extends Component {

   constructor() {
      super();
   }

   render() {
      return (
         <Box>
            <Header size='large' pad={{ horizontal: 'medium' }}>
               <Title responsive={false}>
                  <span>Monitors</span>
               </Title>
            </Header>
            <Section key={'section'} pad='none'>
               <Header size='small' justify='start' responsive={false}
                       separator='top' pad={{ horizontal: 'small' }}>
                  <Label size='small'>Portainer</Label>
               </Header>
               <Tiles flush={false} fill={false} selectable={true}>
                  <Card heading='Applications' separator="all" margin="small"
                        description='Application Management'
                        link={<Anchor href='http://ahc01bac3d:9000' label='ahc01bac3d' target="_blank"/>} />
                  <Card heading='Infrastructure' separator="all" margin="small"
                        description='Infrastructure Management'
                        link={<Anchor href='http://ahc01fro3d:9000' label='ahc01fro3d' target="_blank"/>} />
               </Tiles>
            </Section>
            <Section key={'section'} pad='none'>
               <Header size='small' justify='start' responsive={false}
                       separator='top' pad={{ horizontal: 'small' }}>
                  <Label size='small'>InfluxDB</Label>
               </Header>
               <Tiles flush={false} fill={false} selectable={true}>
                  <Card heading='Grafana' separator="all" margin="small"
                        description='Grafana'
                        link={<Anchor href='http://ahc01fro3d:3000' label='ahc01fro3d' target="_blank"/>} />
                  <Card heading='Chronograf' separator="all" margin="small"
                        description='Chronograf'
                        link={<Anchor href='http://ahc01fro3d:8888' label='ahc01fro3d' target="_blank"/>} />
               </Tiles>
            </Section>
         </Box>
      )
   }
}
